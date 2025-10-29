package com.vitacheck.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitacheck.ai.config.GcpAuthConfig;
import com.vitacheck.ai.dto.AiRecommendationResponseDto;
import com.vitacheck.ai.repository.SupplementCatalogRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiRecommendationService {

    // --- 의존성 주입 ---
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final SupplementCatalogRepository catalogRepository;
    private final GcpAuthConfig.AccessTokenProvider accessTokenProvider;

    @Value("${vertex.api.url}")
    private String vertexApiUrl;

    private String getAccessToken() throws IOException {
        return accessTokenProvider.getAccessToken();
    }

    // --- 내부 데이터 구조 (DB 조회용) ---
    private static class CatalogIndex {
        Map<Long, String> idToName = new HashMap<>();
        Map<Long, List<String>> idToIngredients = new HashMap<>();
        List<Long> idsInOrder                 = new ArrayList<>();
    }

    // --- 내부 데이터 구조 (AI 전달용 DTO) ---
    @Getter
    private static class AiCatalogItem {
        private final long id;
        private final String name;
        private final List<String> ingredients;

        AiCatalogItem(long id, String name, List<String> ingredients) {
            this.id = id;
            this.name = name;
            this.ingredients = ingredients;
        }
    }

    @Getter
    private static class AiKnowledgeItem {
        private final String purpose;
        private final List<String> ingredients;

        AiKnowledgeItem(String purpose, List<String> ingredients) {
            this.purpose = purpose;
            this.ingredients = ingredients;
        }
    }


    // =====================================================================================
    // 외부 호출 메인 메서드 (Controller -> Service)
    // =====================================================================================
    public AiRecommendationResponseDto getRecommendations(List<String> userPurposes) throws IOException {

        // 1. 사용자 목표 정리
        LinkedHashSet<String> goals = new LinkedHashSet<>(
                userPurposes == null ? List.of() :
                        userPurposes.stream()
                                .filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty())
                                .collect(Collectors.toList())
        );
        if (goals.isEmpty()) {
            return new AiRecommendationResponseDto(Collections.emptyList());
        }
        String userGoalsStr = String.join(", ", goals);

        // 2. DB에서 AI에게 전달할 '지식' 조회
        // 2-1. [지식1] 전체 영양제 카탈로그 (ID, 이름, 주요 성분)
        // (주의: findWholeCatalogNative 쿼리 수정 필요 - 컬럼 4개: id, name, purposeCsv, ingredientCsv)
        List<Object[]> allRows = catalogRepository.findWholeCatalogNative();
        CatalogIndex wholeIdx = buildCatalogIndex(allRows);
        if (wholeIdx.idsInOrder.isEmpty()) {
            return new AiRecommendationResponseDto(Collections.emptyList());
        }
        String catalogContext = buildCatalogContext(wholeIdx);

        // 2-2. [지식2] 목적-성분 매핑 정보 (Repository에 추가 필요)
        List<Object[]> knowledgeRows = catalogRepository.findPurposeIngredientKnowledgeNative();
        String knowledgeContext = buildKnowledgeContext(knowledgeRows);

        // 3. AI에게 추천을 요청하는 프롬프트 생성
        String prompt = createRecommendationPrompt(userGoalsStr, catalogContext, knowledgeContext);

        try {
            // 4. AI 호출 (Vertex)
            String rawJsonResponse = callVertex(buildVertexRequestBodyForRecommendation(prompt));
            log.info(">>>>> [디버깅] Vertex AI 원본 응답: {}", rawJsonResponse);

            // 5. AI 응답을 DTO로 파싱
            AiRecommendationResponseDto aiResponse = parseLlmResponse(rawJsonResponse);

            if (aiResponse != null && aiResponse.getRecommendedCombinations() != null) {
                List<AiRecommendationResponseDto.RecommendedCombination> validatedCombinations =
                        aiResponse.getRecommendedCombinations().stream()
                                .filter(combo -> {
                                    boolean hasOverlap = hasIngredientOverlapJavaCheck(combo.getSupplementIds(), wholeIdx);
                                    if (hasOverlap) {
                                        log.warn(">>> AI 추천 조합 ID {} 에서 성분 중복이 발견되어 필터링합니다.", combo.getSupplementIds());
                                    }
                                    return !hasOverlap; // 중복이 없는 것만 통과
                                })
                                .collect(Collectors.toList());

                // 중복 없는 조합만 최종 결과로 설정
                aiResponse.setRecommendedCombinations(validatedCombinations);
                log.info(">>> 최종 추천 조합 (중복 검증 후): {}개", validatedCombinations.size());
            }
            return aiResponse != null ? aiResponse : new AiRecommendationResponseDto(Collections.emptyList()); // null 체크 추가

        } catch (Exception e) {
            log.error("Gemini 추천 생성 실패 (사용자 목적: {}): {}", userGoalsStr, e.getMessage(), e);
            // AI 실패 시 비상 폴백: 빈 리스트 반환
            return new AiRecommendationResponseDto(Collections.emptyList());
        }
    }

    // =====================================================================================
    // AI 상호작용 (프롬프트, 파싱, 요청)
    // =====================================================================================

    /**
     * AI에게 전달할 영양제 카탈로그 JSON 문자열 생성
     */
    private String buildCatalogContext(CatalogIndex wholeIdx) throws JsonProcessingException {
        List<AiCatalogItem> catalogItems = wholeIdx.idsInOrder.stream()
                .map(id -> new AiCatalogItem(
                        id,
                        wholeIdx.idToName.getOrDefault(id, "N/A"),
                        wholeIdx.idToIngredients.getOrDefault(id, Collections.emptyList())
                ))
                .collect(Collectors.toList());

        // [{id: 1, name: "...", ingredients: ["..."]}, ...]
        return objectMapper.writeValueAsString(catalogItems);
    }

    /**
     * AI에게 전달할 목적-성분 지식 JSON 문자열 생성
     */
    private String buildKnowledgeContext(List<Object[]> knowledgeRows) throws JsonProcessingException {
        // [목적명] -> [성분 리스트] 로 그룹화
        Map<String, List<String>> knowledgeMap = knowledgeRows.stream()
                .filter(row -> row[0] != null && row[1] != null)
                .collect(Collectors.groupingBy(
                        row -> (String) row[0], // purpose_name
                        Collectors.mapping(row -> (String) row[1], Collectors.toList()) // ingredient_name
                ));

        List<AiKnowledgeItem> knowledgeItems = knowledgeMap.entrySet().stream()
                .map(entry -> new AiKnowledgeItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // [{purpose: "눈 건강", ingredients: ["루테인", ...]}, ...]
        return objectMapper.writeValueAsString(knowledgeItems);
    }

    /**
     * AI에게 실제 추천을 요청하는 프롬프트 생성
     */
    private String createRecommendationPrompt(String userGoals, String catalogJson, String knowledgeJson) {
        return String.format(
                "당신은 사용자의 건강 목적에 맞춰 영양제를 조합해주는 전문 영양사 AI입니다.\n\n" +
                        "[사용자 건강 목적]\n%s\n\n" +
                        "[추천 가능한 영양제 카탈로그 (각 항목은 `id`, `name`, `ingredients` 포함)]:\n%s\n\n" +
                        "[건강 목적별 주요 성분 지식 (참고용)]:\n%s\n\n" +
                        "[요청 사항]\n" +
                        "1. [사용자 건강 목적] 해결에 가장 도움이 되는 조합을 **최대 3개** 추천해주세요.\n" +
                        "2. 조합은 반드시 [추천 가능한 영양제 카탈로그]에 명시된 `id`만 사용해야 합니다.\n" +
                        "3. (매우 중요!) **각 조합 내 영양제들의 `ingredients` 목록을 모두 합쳤을 때, 동일하거나 유사한 성분 이름이 중복되지 않도록 해주세요.** 예를 들어, 한 제품에 '비타민C'가 있고 다른 제품에 '아스코르빈산'이 있다면 중복으로 간주해야 합니다. 종합비타민처럼 여러 성분이 포함된 제품을 조합할 때 특히 주의해주세요.\n" + // <-- 중복 관련 지시 구체화
                        "4. 사용자의 모든 목적을 1순위로 고려하되, 달성하기 어렵다면 가장 중요한 목적 1~2개를 중심으로 조합해도 좋습니다.\n" +
                        "5. 각 조합의 `reason`은 **2~3 문장 이내로 간결하게**, 어떤 영양제(이름 언급)의 어떤 성분(`ingredients` 목록 참고)이 사용자의 어떤 목적에 기여하는지 명확히 설명해주세요.\n" +
                        "6. 응답은 다른 설명이나 markdown(` ```json `) 없이, 아래 [출력 형식]과 정확히 일치하는 순수 JSON 객체로만 생성해주세요.\n\n" +
                        "[출력 형식]\n" +
                        "{\n" +
                        "  \"recommendedCombinations\": [\n" +
                        "    {\n" +
                        "      \"combinationName\": \"눈 피로 집중 케어 조합\",\n" +
                        "      \"supplementIds\": [15, 22],\n" +
                        "      \"reason\": \"눈 건강의 핵심인 루테인과 피로 회복을 돕는 비타민B군을 함께 섭취하여 시너지 효과를 낼 수 있는 조합입니다.\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n",
                userGoals, catalogJson, knowledgeJson
        );
    }

    /**
     * AI의 전체 추천 응답을 파싱
     */
    private AiRecommendationResponseDto parseLlmResponse(String jsonResponse) throws JsonProcessingException {
        try {
            Map<String, Object> fullResponse = objectMapper.readValue(jsonResponse, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) fullResponse.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                log.warn("AI 응답에 'candidates' 필드가 없습니다.");
                return new AiRecommendationResponseDto(Collections.emptyList());
            }

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            if (content == null) return new AiRecommendationResponseDto(Collections.emptyList());

            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) return new AiRecommendationResponseDto(Collections.emptyList());

            String textPayload = (String) parts.get(0).get("text");
            if (textPayload == null) return new AiRecommendationResponseDto(Collections.emptyList());

            String payload = stripToJsonEnvelope(textPayload);

            // AI가 생성한 JSON을 AiRecommendationResponseDto 객체로 바로 변환
            return objectMapper.readValue(payload, AiRecommendationResponseDto.class);

        } catch (Exception e) {
            log.warn("AI 응답 파싱 실패, 비어있는 추천 반환: {}", e.getMessage());
            throw new JsonProcessingException("AI 응답 파싱 실패", e) {};
        }
    }

    /**
     * AI 추천을 위한 Vertex 요청 Body 생성
     */
    private Map<String, Object> buildVertexRequestBodyForRecommendation(String prompt) {

        // AI가 반환해야 할 JSON 스키마를 AiRecommendationResponseDto에 맞게 정의
        Map<String, Object> comboSchema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "combinationName", Map.of("type", "STRING"),
                        "supplementIds", Map.of("type", "ARRAY", "items", Map.of("type", "NUMBER")),
                        "reason", Map.of("type", "STRING")
                ),
                "required", List.of("combinationName", "supplementIds", "reason")
        );

        Map<String, Object> responseSchema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "recommendedCombinations", Map.of(
                                "type", "ARRAY",
                                "items", comboSchema
                        )
                ),
                "required", List.of("recommendedCombinations")
        );

        return Map.of(
                "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "maxOutputTokens", 8192, // (응답 토큰을 넉넉하게 설정)
                        "temperature", 0.4,   // (창의성/일관성 조절)
                        "response_mime_type", "application/json",
                        "response_schema", responseSchema
                )
        );
    }

    private String callVertex(Map<String, Object> body) throws IOException {
        String token = getAccessToken();

        WebClient client = webClientBuilder
                .baseUrl(vertexApiUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();

        try {
            return client.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // (실제 프로덕션에서는 .block() 대신 비동기 처리를 권장합니다)
        } catch (Exception e) {
            throw new IOException("Vertex 호출 실패: " + e.getMessage(), e);
        }
    }

    // =====================================================================================
    // 유틸리티 및 헬퍼 메서드
    // =====================================================================================
    private CatalogIndex buildCatalogIndex(List<Object[]> rows) {
        CatalogIndex idx = new CatalogIndex();
        if (rows == null) return idx;
        for (Object[] r : rows) {
            try {
                Long   id            = ((Number) r[0]).longValue();
                String name          = (String) r[1];
                // String purposeCsv    = (String) r[2]; // (참고: 인덱스 2번이 purposeCsv로 가정)
                String ingredientCsv = (String) r[3]; // (참고: 인덱스 3번이 ingredientCsv로 가정)

                idx.idToName.put(id, name);
                idx.idToIngredients.put(id, parseCsv(ingredientCsv));
                idx.idsInOrder.add(id);
            } catch (Exception e) {
                log.warn("카탈로그 인덱스 생성 중 오류 발생 (행 데이터: {}): {}", Arrays.toString(r), e.getMessage());
            }
        }
        return idx;
    }

    private List<String> parseCsv(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptyList();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String stripToJsonEnvelope(String contentText) {
        String s = contentText == null ? "" : contentText.trim();
        if (s.startsWith("```json")) s = s.substring(7);
        if (s.startsWith("```")) s = s.substring(3);
        if (s.endsWith("```")) s = s.substring(0, s.length() - 3);

        int first = s.indexOf('{');
        int last  = s.lastIndexOf('}');
        if (first >= 0 && last > first) {
            return s.substring(first, last + 1).trim();
        }
        return "{}"; // Fallback to empty JSON object
    }

    /**
     * Java 코드로 영양제 조합 내 성분 중복 여부를 확인합니다.
     * @param supplementIds 검사할 영양제 ID 목록
     * @param wholeIdx 전체 영양제 성분 정보가 담긴 인덱스
     * @return 중복이 있으면 true, 없으면 false
     */
    private boolean hasIngredientOverlapJavaCheck(List<Integer> supplementIds, CatalogIndex wholeIdx) {
        if (supplementIds == null || supplementIds.size() < 2 || wholeIdx == null || wholeIdx.idToIngredients == null) {
            return false; // 비교 대상이 없으면 중복 아님
        }
        Set<String> seenIngredients = new HashSet<>();
        for (Integer supplementId : supplementIds) {
            if (supplementId == null) continue;
            // wholeIdx에서 해당 영양제의 성분 목록을 가져옴
            List<String> ingredients = wholeIdx.idToIngredients.getOrDefault(supplementId.longValue(), Collections.emptyList());

            for (String ingredient : ingredients) {
                String normalizedIngredient = normalizeIngredientName(ingredient); // 성분 이름 정규화
                if (!normalizedIngredient.isEmpty()) {
                    // 이미 Set에 존재하면 중복 발견
                    if (!seenIngredients.add(normalizedIngredient)) {
                        log.debug(">>> 성분 중복 감지! 조합: {}, 중복 성분: '{}' (원문: '{}')", supplementIds, normalizedIngredient, ingredient);
                        return true; // 중복 발견 즉시 true 반환
                    }
                }
            }
        }
        return false; // 루프를 다 돌았는데 중복 없으면 false 반환
    }

    /**
     * 성분 이름을 비교하기 쉽도록 정규화합니다.
     * 예: " 비타민 B1 (티아민) " -> "비타민b1", "오메가3 (EPA 및 DHA 함유 유지)" -> "오메가3"
     * 실제 데이터셋을 기반으로 동의어 처리를 강화합니다.
     * @param name 원본 성분 이름
     * @return 정규화된 대표 성분 이름 (소문자, 주요 명칭 위주)
     */
    private String normalizeIngredientName(String name) {
        if (name == null) return "";

        String lowerCaseName = name.trim().toLowerCase();

        // 1. 괄호 안 내용 제거 (부가 설명 제거 목적)
        // "오메가3 (epa 및 dha 함유 유지)" -> "오메가3"
        // "비타민 b1 (티아민)" -> "비타민 b1"
        lowerCaseName = lowerCaseName.replaceAll("\\s*\\(.*?\\)\\s*", "").trim();

        // 2. 특수문자(하이픈 등) 및 연속 공백을 단일 공백으로 처리 (완전 제거 대신)
        // "비타민 b-1", "비타민 b 1" -> "비타민 b 1" (이후 동의어 처리에서 "비타민b1"로 변환)
        // 불필요한 특수기호 제거는 유지하되, 공백은 남겨서 단어 구분을 유지
        lowerCaseName = lowerCaseName.replaceAll("[^a-z0-9가-힣\\s]", "").replaceAll("\\s+", " ").trim();

        // 3. 동의어 처리 (Map 사용)
        //    성능을 위해 Map은 클래스 멤버 변수로 선언하고 static 초기화 블록에서 생성하는 것이 더 좋음
        Map<String, String> synonymMap = createSynonymMap(); // 헬퍼 메서드 사용

        // 정규화된 이름 또는 공백 제거된 이름이 동의어 Map의 키에 해당하면 대표 이름으로 변환
        String key1 = lowerCaseName; // 공백 포함 키 (예: "비타민 b1")
        String key2 = lowerCaseName.replaceAll("\\s", ""); // 공백 제거 키 (예: "비타민b1")

        if (synonymMap.containsKey(key1)) {
            return synonymMap.get(key1);
        } else if (synonymMap.containsKey(key2)) {
            return synonymMap.get(key2);
        } else {
            // 동의어 맵에 없으면 공백 제거한 형태를 최종 반환 (예: "비타민c", "오메가3")
            return key2;
        }
    }

    // 동의어 Map을 생성하는 헬퍼 메서드 (클래스 초기화 시 한 번만 실행되도록 static 멤버로 빼는 것을 권장)
    private static Map<String, String> createSynonymMap() {
        Map<String, String> map = new HashMap<>();
        // 비타민 C
        map.put("비타민c", "비타민c");
        map.put("비타민 c", "비타민c");
        map.put("아스코르빈산", "비타민c");
        map.put("아스코브산", "비타민c");
        // 비타민 B1
        map.put("비타민b1", "비타민b1");
        map.put("비타민 b1", "비타민b1");
        map.put("티아민", "비타민b1");
        // 비타민 B2
        map.put("비타민b2", "비타민b2");
        map.put("비타민 b2", "비타민b2");
        map.put("리보플라빈", "비타민b2");
        // 비타민 B3
        map.put("비타민b3", "비타민b3");
        map.put("비타민 b3", "비타민b3");
        map.put("나이아신", "비타민b3");
        map.put("니코틴산", "비타민b3");
        // 비타민 B5
        map.put("비타민b5", "비타민b5");
        map.put("비타민 b5", "비타민b5");
        map.put("판토텐산", "비타민b5");
        // 비타민 B6
        map.put("비타민b6", "비타민b6");
        map.put("비타민 b6", "비타민b6");
        map.put("피리독신", "비타민b6");
        // 비타민 B7
        map.put("비타민b7", "비타민b7");
        map.put("비타민 b7", "비타민b7");
        map.put("비오틴", "비타민b7"); // 비오틴을 대표명으로 써도 무방
        // 비타민 B9
        map.put("비타민b9", "비타민b9");
        map.put("비타민 b9", "비타민b9");
        map.put("엽산", "비타민b9");
        // 비타민 B12
        map.put("비타민b12", "비타민b12");
        map.put("비타민 b12", "비타민b12");
        map.put("코발라민", "비타민b12");
        map.put("시아노코발라민", "비타민b12");
        // 비타민 A
        map.put("비타민a", "비타민a");
        map.put("비타민 a", "비타민a");
        map.put("레티놀", "비타민a");
        // 비타민 D
        map.put("비타민d", "비타민d");
        map.put("비타민 d", "비타민d");
        map.put("칼시페롤", "비타민d");
        // 비타민 E
        map.put("비타민e", "비타민e");
        map.put("비타민 e", "비타민e");
        map.put("토코페롤", "비타민e");
        // 오메가3 관련 (괄호 제거 후 처리 예시)
        map.put("오메가3", "오메가3");
        map.put("epa및dha함유유지", "오메가3"); // 괄호 제거 후 남는 문자열
        map.put("epa dha", "오메가3"); // 다른 표기 가능성
        // 프로바이오틱스
        map.put("프로바이오틱스", "프로바이오틱스");
        map.put("유산균", "프로바이오틱스"); // 유산균도 프로바이오틱스로 통일

        // !!! 중요: 실제 ingredients.csv 파일을 전체적으로 보고 더 많은 동의어와 표기법(띄어쓰기 등)을 추가해야 함 !!!
        return Collections.unmodifiableMap(map); // 변경 불가능한 Map으로 반환
    }
}