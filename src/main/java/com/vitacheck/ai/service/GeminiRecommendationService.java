package com.vitacheck.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitacheck.ai.config.GcpAuthConfig;
import com.vitacheck.ai.dto.AiRecommendationResponseDto;
import com.vitacheck.ai.repository.SupplementCatalogRepository;
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

    // --- 내부 데이터 구조 ---
    private static class CatalogIndex {
        Map<Long, String> idToName = new HashMap<>();
        Map<Long, List<String>> idToIngredients = new HashMap<>();
        Map<Long, Set<String>> idToPurposes   = new HashMap<>();
        List<Long> idsInOrder                 = new ArrayList<>();
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

        // 2. DB에서 데이터 조회 및 인덱스 생성
        List<String> purposeKeys = normalizePurposeKeys(goals);
        List<Object[]> targetedRows = catalogRepository.findCatalogForPurposesNative(purposeKeys);
        CatalogIndex targetedIdx = buildCatalogIndex(targetedRows);

        List<Object[]> allRows = catalogRepository.findWholeCatalogNative();
        CatalogIndex wholeIdx = buildCatalogIndex(allRows);
        if (wholeIdx.idsInOrder.isEmpty()) {
            return new AiRecommendationResponseDto(Collections.emptyList());
        }

        // 3. DB 기반으로 견고한 조합 생성
        List<AiRecommendationResponseDto.RecommendedCombination> dbCombos =
                buildCombinationsRobust(goals, targetedIdx, wholeIdx);

        // 4. 각 조합을 AI에게 보내 이름과 이유를 자연스럽게 다듬기
        for (AiRecommendationResponseDto.RecommendedCombination combo : dbCombos) {
            try {
                String prompt = createEnrichmentPrompt(combo, goals, wholeIdx);
                String rawJsonResponse = callVertex(buildVertexRequestBodyForEnrichment(prompt));
                updateComboWithLlmResponse(combo, rawJsonResponse);
            } catch (Exception e) {
                log.warn("LLM 보강 실패 (조합 ID: {}) – DB 템플릿 사용: {}", combo.getSupplementIds(), e.getMessage());
            }
        }

        return new AiRecommendationResponseDto(dbCombos);
    }


    // =====================================================================================
    // 조합 생성 핵심 로직 (최종 수정본)
    // =====================================================================================
    private List<AiRecommendationResponseDto.RecommendedCombination> buildCombinationsRobust(
            Set<String> goals, CatalogIndex targetedIdx, CatalogIndex wholeIdx) {

        // 1. 각 목적별로 상위 5개의 후보 영양제 목록을 만듭니다.
        Map<String, List<Long>> candidatesByGoal = new LinkedHashMap<>();
        for (String goal : goals) {
            List<Long> candidates = targetedIdx.idsInOrder.stream()
                    .filter(id -> targetedIdx.idToPurposes.getOrDefault(id, Collections.emptySet())
                            .stream().anyMatch(p -> matchesGoal(goal, p)))
                    .limit(5)
                    .collect(Collectors.toList());

            // 만약 타겟 카탈로그에서 후보를 못찾으면, 전체 카탈로그에서 다시 검색
            if (candidates.isEmpty()) {
                candidates = wholeIdx.idsInOrder.stream()
                        .filter(id -> wholeIdx.idToPurposes.getOrDefault(id, Collections.emptySet())
                                .stream().anyMatch(p -> matchesGoal(goal, p)))
                        .limit(5)
                        .collect(Collectors.toList());
            }
            if (!candidates.isEmpty()) {
                candidatesByGoal.put(goal, candidates);
            }
        }
        log.info(">>>>> [디버깅] 목적별 후보군: {}", candidatesByGoal);

        List<AiRecommendationResponseDto.RecommendedCombination> results = new ArrayList<>();
        Set<String> seenCombos = new HashSet<>(); // 조합 중복 방지용
        List<String> goalList = new ArrayList<>(candidatesByGoal.keySet());

        // 2. [우선순위 1] 3개 조합 생성 시도
        if (goalList.size() >= 3) {
            // 각 목적별 1순위 후보들을 가져옴
            Long sup1 = candidatesByGoal.get(goalList.get(0)).get(0);
            Long sup2 = candidatesByGoal.get(goalList.get(1)).get(0);
            Long sup3 = candidatesByGoal.get(goalList.get(2)).get(0);

            // 중복되지 않는 3개의 고유한 영양제를 찾기 위한 노력
            Set<Long> uniqueThree = new LinkedHashSet<>();
            uniqueThree.add(sup1);
            uniqueThree.add(sup2);
            uniqueThree.add(sup3);

            // 만약 중복이 있다면, 2,3순위 후보를 사용하여 채움
            if (uniqueThree.size() < 3) {
                for (String goal : goalList) {
                    for (Long candidateId : candidatesByGoal.get(goal)) {
                        uniqueThree.add(candidateId);
                        if (uniqueThree.size() >= 3) break;
                    }
                    if (uniqueThree.size() >= 3) break;
                }
            }

            if (uniqueThree.size() >= 3) {
                List<Integer> triplet = uniqueThree.stream().map(Long::intValue).collect(Collectors.toList());
                String key = triplet.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
                if (seenCombos.add(key)) {
                    AiRecommendationResponseDto.RecommendedCombination rc = new AiRecommendationResponseDto.RecommendedCombination();
                    rc.setCombinationName(fallbackNameFor(goalList.get(0)) + " & 종합 케어");
                    rc.setSupplementIds(triplet);
                    rc.setReason(String.join(", ", toNameReason(goalList.get(0)), toNameReason(goalList.get(1)).toLowerCase(), toNameReason(goalList.get(2)).toLowerCase()));
                    results.add(rc);
                }
            }
        }

        // 3. [우선순위 2] 2개 조합 생성 (최대 3개가 될 때까지)
        if (goalList.size() >= 2) {
            for (int i = 0; i < goalList.size(); i++) {
                for (int j = i + 1; j < goalList.size(); j++) {
                    if (results.size() >= 3) break;

                    String goal1 = goalList.get(i);
                    String goal2 = goalList.get(j);

                    // 각 목적의 후보군에서 아직 사용되지 않은 최상위 영양제를 하나씩 선택
                    Optional<Long> sup1Opt = candidatesByGoal.get(goal1).stream().findFirst();
                    Optional<Long> sup2Opt = candidatesByGoal.get(goal2).stream().findFirst();

                    if (sup1Opt.isPresent() && sup2Opt.isPresent()) {
                        List<Integer> pair = List.of(sup1Opt.get().intValue(), sup2Opt.get().intValue());
                        String key = pair.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));

                        if (seenCombos.add(key) && !hasIngredientOverlap(pair, wholeIdx)) {
                            AiRecommendationResponseDto.RecommendedCombination rc = new AiRecommendationResponseDto.RecommendedCombination();
                            rc.setCombinationName(fallbackNameFor(goal1) + " & " + fallbackNameFor(goal2));
                            rc.setSupplementIds(pair);
                            rc.setReason(toNameReason(goal1) + ", 그리고 " + toNameReason(goal2).toLowerCase());
                            results.add(rc);
                        }
                    }
                }
                if (results.size() >= 3) break;
            }
        }

        // 4. [우선순위 3] 단일 목적 폴백 (조합이 하나도 없을 경우)
        if (results.isEmpty() && !goalList.isEmpty()) {
            String goal = goalList.get(0);
            List<Long> candidates = candidatesByGoal.get(goal);

            // 후보군 내에서 성분이 다른 2개를 찾아 조합
            for (int i = 0; i < candidates.size(); i++) {
                for (int j = i + 1; j < candidates.size(); j++) {
                    List<Integer> pair = List.of(candidates.get(i).intValue(), candidates.get(j).intValue());
                    if (!hasIngredientOverlap(pair, wholeIdx)) {
                        AiRecommendationResponseDto.RecommendedCombination rc = new AiRecommendationResponseDto.RecommendedCombination();
                        rc.setCombinationName(fallbackNameFor(goal) + " 맞춤 조합");
                        rc.setSupplementIds(pair);
                        rc.setReason(toNameReason(goal) + " 및 시너지 효과를 위한 조합입니다.");
                        results.add(rc);
                        return results; // 하나만 찾으면 바로 반환
                    }
                }
            }
            // 2개 조합 못찾으면 단일 추천
            AiRecommendationResponseDto.RecommendedCombination rc = new AiRecommendationResponseDto.RecommendedCombination();
            rc.setCombinationName(fallbackNameFor(goal) + " 추천");
            rc.setSupplementIds(List.of(candidates.get(0).intValue()));
            rc.setReason(toNameReason(goal));
            results.add(rc);
        }

        log.info(">>>>> [디버깅] 최종 조합 결과: {}", results);
        return results;
    }


    // =====================================================================================
    // AI 상호작용 (프롬프트, 파싱, 요청)
    // =====================================================================================
    private String createEnrichmentPrompt(AiRecommendationResponseDto.RecommendedCombination combo, Set<String> goals, CatalogIndex idx) {
        String supplementDetails = combo.getSupplementIds().stream()
                .map(id -> {
                    long longId = id.longValue();
                    String name = idx.idToName.getOrDefault(longId, "영양제 (ID: " + longId + ")");
                    List<String> ingredients = idx.idToIngredients.getOrDefault(longId, List.of());
                    return String.format("- %s (주요 성분: %s)", name, String.join(", ", ingredients));
                })
                .collect(Collectors.joining("\n"));

        return String.format(
                "사용자의 건강 목적은 '%s'입니다. 아래 영양제 조합에 대한 추천 이름과 추천 이유를 생성해주세요.\n\n" +
                        "조합:\n%s\n\n" +
                        "규칙:\n" +
                        "1. 'combinationName'과 'reason' 필드만 있는 JSON 객체로만 답변하세요. (절대 다른 텍스트나 markdown 없이 순수 JSON만 출력)\n" +
                        "2. 'combinationName'은 15자 내외의 직관적이고 매력적인 한글 이름이어야 합니다.\n" +
                        "3. 'reason'은 각 영양제가 사용자의 목적에 어떻게 기여하는지 1~2 문장의 자연스러운 한글 설명이어야 합니다.",
                String.join(", ", goals), supplementDetails
        );
    }

    private void updateComboWithLlmResponse(AiRecommendationResponseDto.RecommendedCombination combo, String jsonResponse) {
        try {
            Map<String, Object> fullResponse = objectMapper.readValue(jsonResponse, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) fullResponse.get("candidates");
            if (candidates == null || candidates.isEmpty()) return;

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            if (content == null) return;

            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) return;

            String textPayload = (String) parts.get(0).get("text");
            if (textPayload == null) return;

            String payload = stripToJsonEnvelope(textPayload);
            Map<String, String> enrichedData = objectMapper.readValue(payload, Map.class);

            String name = enrichedData.get("combinationName");
            String reason = enrichedData.get("reason");

            if (name != null && !name.isBlank()) combo.setCombinationName(name);
            if (reason != null && !reason.isBlank()) combo.setReason(reason);

        } catch (Exception e) {
            log.warn("AI 응답 파싱 실패, 템플릿 유지: {}", e.getMessage());
        }
    }

    private Map<String, Object> buildVertexRequestBodyForEnrichment(String prompt) {
        Map<String, Object> responseSchema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "combinationName", Map.of("type", "STRING"),
                        "reason", Map.of("type", "STRING")
                ),
                "required", List.of("combinationName", "reason")
        );

        return Map.of(
                "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "maxOutputTokens", 256,
                        "temperature", 0.3,
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
                    .block();
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
            Long   id            = ((Number) r[0]).longValue();
            String name          = (String) r[1];
            String purposeCsv    = (String) r[2];
            String ingredientCsv = (String) r[3];

            idx.idToName.put(id, name);
            idx.idToIngredients.put(id, parseCsv(ingredientCsv));
            idx.idToPurposes.put(id, new HashSet<>(parseCsv(purposeCsv)));
            idx.idsInOrder.add(id);
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

    private boolean hasIngredientOverlap(List<Integer> ids, CatalogIndex idx) {
        Set<String> seen = new HashSet<>();
        if (idx == null || idx.idToIngredients == null) return false;
        for (Integer i : ids) {
            if (i == null) continue;
            List<String> list = idx.idToIngredients.getOrDefault(i.longValue(), List.of());
            for (String raw : list) {
                String n = normIng(raw);
                if (n.isEmpty()) continue;
                if (seen.contains(n)) return true;
                seen.add(n);
            }
        }
        return false;
    }

    private String normIng(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase().replace("-", " ").replaceAll("\\s+", " ").trim();
    }

    private boolean matchesGoal(String goal, String purpose) {
        if (goal == null || purpose == null) return false;
        String g = goal.trim().toLowerCase();
        String p = purpose.trim().toLowerCase();
        if (g.isEmpty() || p.isEmpty()) return false;

        if (g.contains("피로") && p.contains("피로")) return true;
        if (g.contains("면역") && p.contains("면역")) return true;

        return g.equals(p) || g.contains(p) || p.contains(g);
    }

    private List<String> normalizePurposeKeys(LinkedHashSet<String> goals) {
        return goals.stream()
                .map(s -> s == null ? "" : s.trim().toLowerCase())
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String fallbackNameFor(String goal) {
        String g = (goal == null ? "" : goal.trim());
        if (g.isEmpty()) return "건강 시너지 조합";

        String representativeGoal = g.split(" & ")[0];

        if (representativeGoal.contains("건강")) {
            return representativeGoal;
        }
        if (representativeGoal.endsWith("감")) {
            return representativeGoal.substring(0, representativeGoal.length() - 1) + " 개선";
        }
        return representativeGoal;
    }

    private String toNameReason(String goal) {
        String g = (goal == null ? "" : goal.trim());
        if (g.isEmpty()) return "주요 건강 목표 달성에 도움을 줍니다";

        if (g.endsWith("감")) {
            return g.substring(0, g.length() - 1) + " 개선에 도움을 줍니다";
        }
        if (g.contains("혈당") || g.contains("혈압") || g.contains("콜레스테롤")) {
            return g + " 관리에 도움을 줍니다";
        }
        return g + "에 도움을 줍니다";
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
}