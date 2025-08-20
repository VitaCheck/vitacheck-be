package com.vitacheck.service;

import com.querydsl.core.Tuple;
import com.vitacheck.config.jwt.CustomUserDetails;
import com.vitacheck.domain.Brand;
import com.vitacheck.domain.Ingredient;
import com.vitacheck.domain.IngredientDosage;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.mapping.SupplementIngredient;
import com.vitacheck.domain.purposes.AllPurpose;
import com.vitacheck.domain.purposes.PurposeCategory;
import com.vitacheck.domain.searchLog.SearchCategory;
import com.vitacheck.domain.user.Gender;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.*;
import com.vitacheck.global.apiPayload.CustomException;
import com.vitacheck.global.apiPayload.code.ErrorCode;
import com.vitacheck.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplementService {

    private final SupplementRepository supplementRepository;
    private final IngredientRepository ingredientRepository;
    private final PurposeCategoryRepository purposeCategoryRepository;
    private final SearchLogService searchLogService;
    private final StatisticsService statisticsService;
    private final SupplementLikeRepository supplementLikeRepository;
    private final IngredientDosageRepository dosageRepository;
//    private final PurposeQueryRepository purposeQueryRepository;
    private final BrandRepository brandRepository;

    public SearchDto.UnifiedSearchResponse search(User user, String keyword, String brandName, String ingredientName, Pageable pageable) {

        List<SearchDto.IngredientInfo> matchedIngredients = findMatchedIngredients(keyword, ingredientName);

        Page<Supplement> supplementsPage = supplementRepository.search(user, keyword, brandName, ingredientName, pageable);
        Page<SupplementDto.SearchResponse> supplementDtos = supplementsPage.map(SupplementDto.SearchResponse::from);


        if (StringUtils.hasText(keyword)) {
            keyword=keyword;
        }
        else if (StringUtils.hasText(ingredientName)) {
            keyword=ingredientName;
        }
        else if (StringUtils.hasText(brandName)) {
            keyword=brandName;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // 🔹 검색 로그 저장(미로그인)
            searchLogService.logSearch(null, keyword, SearchCategory.KEYWORD, null,null);
        } else {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            user = userDetails.getUser();
            LocalDate birthDate = user.getBirthDate();
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            // 🔹 검색 로그 저장(로그인)
            searchLogService.logSearch(user.getId(), keyword, SearchCategory.KEYWORD, age, user.getGender());
        }


        return SearchDto.UnifiedSearchResponse.builder()
                .matchedIngredients(matchedIngredients)
                .supplements(supplementDtos)
                .build();
    }


    private List<SearchDto.IngredientInfo> findMatchedIngredients(String keyword, String ingredientName) {
        // 1. 실제 검색에 사용할 검색어를 정합니다. ingredientName이 우선순위를 가집니다.
        String finalSearchTerm = StringUtils.hasText(ingredientName) ? ingredientName : keyword;

        // 2. 검색어가 존재하는 경우, 이름을 포함하는 모든 성분을 검색합니다.
        if (StringUtils.hasText(finalSearchTerm)) {
            return ingredientRepository.findByNameContainingIgnoreCase(finalSearchTerm).stream()
                    .map(SearchDto.IngredientInfo::from)
                    .collect(Collectors.toList());
        }

        // 3. 검색어가 없으면 빈 리스트를 반환합니다.
        return Collections.emptyList();
    }

//    @Transactional(readOnly = true)
//    public Slice<IngredientPurposeBucket> getSupplementsByPurposesPaged(SupplementPurposeRequest request,
//                                                                        Pageable pageable) {
//        // 1) 요청 enum 파싱
//        List<AllPurpose> purposes = request.getPurposeNames().stream()
//                .map(String::trim)
//                .filter(s -> !s.isEmpty())
//                .map(AllPurpose::valueOf)
//                .distinct()
//                .toList();
//
//        // 2) 얇은 페이지: 성분 ID만
//        Slice<Long> ingredientSlice = purposeQueryRepository.findIngredientIdPageByPurposes(purposes, pageable);
//        List<Long> ingredientIds = ingredientSlice.getContent();
//
//        if (ingredientIds.isEmpty()) {
//            return new SliceImpl<>(Collections.emptyList(), pageable, false);
//        }
//
//        // 3) 목적과 보충제는 각각 가볍게 조회
//        Map<Long, List<AllPurpose>> purposeMap =
//                purposeQueryRepository.findPurposesByIngredientIds(ingredientIds, purposes);
//        Map<Long, PurposeQueryRepositoryImpl.SupplementBriefRow> dummy = null; // import 참고
//        Map<Long, List<PurposeQueryRepositoryImpl.SupplementBriefRow>> supplementMap =
//                purposeQueryRepository.findSupplementsByIngredientIds(ingredientIds);
//
//        // 성분명 조회
//        Map<Long, String> ingredientNames = purposeQueryRepository.findIngredientNames(ingredientIds);
//
//        // 4) DTO 조립 (순서: 페이지 순서를 그대로 유지)
//        //    description 캐시로 valueOf/getDescription 반복 비용 절감
//        Map<AllPurpose, String> descCache = Arrays.stream(AllPurpose.values())
//                .collect(Collectors.toMap(p -> p, AllPurpose::getDescription));
//
//        List<IngredientPurposeBucket> items = new ArrayList<>(ingredientIds.size());
//        for (Long ingId : ingredientIds) {
//            String ingName = ingredientNames.getOrDefault(ingId, "");
//
//            List<String> purposesDesc = purposeMap.getOrDefault(ingId, List.of()).stream()
//                    .map(descCache::get)
//                    .distinct()
//                    .toList();
//
//            List<SupplementByPurposeResponse.SupplementBrief> supplements =
//                    supplementMap.getOrDefault(ingId, List.of()).stream()
//                            .map(r -> SupplementByPurposeResponse.SupplementBrief.builder()
//                                    .id(r.getSupplementId())
//                                    .name(r.getSupplementName())
//                                    .imageUrl(r.getSupplementImageUrl())
//                                    .build())
//                            .toList();
//
//            items.add(IngredientPurposeBucket.builder()
//                    .ingredientName(ingName)
//                    .data(SupplementByPurposeResponse.builder()
//                            .id(ingId)
//                            .purposes(purposesDesc)
//                            .supplements(supplements)
//                            .build())
//                    .build());
//        }
//
//        return new SliceImpl<>(items, pageable, ingredientSlice.hasNext());
//    }


    @Transactional(readOnly = true)
    public SupplementDetailResponseDto getSupplementDetail(Long supplementId, Long userId) {
        Supplement supplement = supplementRepository.findById(supplementId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUPPLEMENT_NOT_FOUND));

        Brand brand = supplement.getBrand();

        boolean liked = (userId != null) && supplementLikeRepository.existsByUserIdAndSupplementId(userId, supplementId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // 🔹 클릭 로그 저장 (미로그인)
            searchLogService.logClick(null, supplement.getName(), SearchCategory.SUPPLEMENT, null,null);

        } else {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            LocalDate birthDate = user.getBirthDate();
            int age = Period.between(birthDate, LocalDate.now()).getYears();

            // 🔹 클릭 로그 저장 (로그인)
            searchLogService.logClick(user.getId(), supplement.getName(), SearchCategory.SUPPLEMENT, age, user.getGender());
        }

        return SupplementDetailResponseDto.builder()
                .supplementId(supplement.getId())
                .brandId(brand != null ? brand.getId() : null)
                .brandName(supplement.getBrand().getName())
                .brandImageUrl(supplement.getBrand().getImageUrl())
                .supplementName(supplement.getName())
                .supplementImageUrl(supplement.getImageUrl())
                .liked(liked)
                .coupangLink(supplement.getCoupangUrl())
                .intakeTime(supplement.getMethod())
                .ingredients(
                        supplement.getSupplementIngredients().stream()
                                .map(i -> {
                                    // Ingredient를 통해 unit을 가져오는 것이 아니라,
                                    // SupplementIngredient 자체의 unit을 사용하도록 수정
                                    String amount = (i.getAmount() != null) ? i.getAmount().toString() : "";
                                    // *** SupplementIngredient에 unit이 없으므로 Ingredient에서 가져와야 함 ***
                                    String unit = i.getIngredient().getUnit() != null ? i.getIngredient().getUnit() : "";

                                    return new SupplementDetailResponseDto.IngredientDto(
                                            i.getIngredient().getName(),
                                            amount + unit
                                    );
                                })
                                .toList())
                .build();
    }


    public List<SupplementDto.SimpleResponse> getSupplementsByBrandId(Long brandId) {
        // 이 메소드는 변경 없음
        return supplementRepository.findAllByBrandId(brandId).stream()
                .map(SupplementDto.SimpleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplementDto.DetailResponse getSupplementDetailById(Long id) {

        // 1) 영양제 + 성분 한 번에 조회
        Supplement supplement = supplementRepository.findByIdWithBrandAndIngredients(id)
                .orElseThrow(() -> new RuntimeException("해당 영양제를 찾을 수 없습니다."));

        // 2) 성분 ID 수집
        Set<Long> ingredientIds = supplement.getSupplementIngredients().stream()
                .map(si -> si.getIngredient().getId())
                .collect(Collectors.toSet());

        // 3) 사용자 성별/나이
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Gender gender = Gender.ALL;
        Integer age = null;

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            User user = userDetails.getUser();
            gender = (user.getGender() != null) ? user.getGender() : Gender.ALL;

            if (user.getBirthDate() != null) {
                age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();
            }

            // 클릭 로그(로그인)
            searchLogService.logClick(user.getId(), supplement.getName(), SearchCategory.SUPPLEMENT, age, gender);
        } else {
            // 클릭 로그(비로그인)
            searchLogService.logClick(null, supplement.getName(), SearchCategory.SUPPLEMENT, null, null);
        }

        // 나이 미확정이면 성인 기본값(25세) 가정
        if (age == null) age = 25;

        // 4) 권장량/UL 로딩: 유저조건 → 일반값(ALL & 무연령) 보강
        Map<Long, IngredientDosage> dosageByIngredientId = new HashMap<>();

        // 4-1) 유저 성별/나이에 맞는 도수 우선 채움
        List<IngredientDosage> userDosages =
                dosageRepository.findDosagesByUserCondition(ingredientIds, gender, age);
        userDosages.forEach(d -> dosageByIngredientId.put(d.getIngredient().getId(), d));

        // 4-2) 못 채운 성분은 일반값으로 보강
        Set<Long> missingIds = ingredientIds.stream()
                .filter(ingId -> !dosageByIngredientId.containsKey(ingId))
                .collect(Collectors.toSet());
        if (!missingIds.isEmpty()) {
            dosageRepository.findGeneralDosagesByIngredientIds(missingIds)
                    .forEach(d -> dosageByIngredientId.putIfAbsent(d.getIngredient().getId(), d));
        }

        // 5) 응답 매핑 (upper=100% 기준)
        List<SupplementDto.DetailResponse.IngredientDetail> ingredients =
                supplement.getSupplementIngredients().stream()
                        .map(si -> {
                            var ing = si.getIngredient();
                            var dosage = dosageByIngredientId.get(ing.getId());

                            double amount = si.getAmount() != null ? si.getAmount() : 0.0;
                            String unit = ing.getUnit() != null ? ing.getUnit() : "";

                            double recommended = (dosage != null && dosage.getRecommendedDosage() != null)
                                    ? dosage.getRecommendedDosage() : 0.0;
                            double upper = (dosage != null && dosage.getUpperLimit() != null)
                                    ? dosage.getUpperLimit() : 0.0;

                            double normalized = (upper > 0.0) ? (amount / upper) * 100.0 : 0.0;
                            double recommendedStart = (upper > 0.0 && recommended > 0.0)
                                    ? (recommended / upper) * 100.0 : 0.0;

                            normalized = Math.min(normalized, 999.0);
                            recommendedStart = Math.min(recommendedStart, 999.0);

                            String status = normalized < recommendedStart ? "deficient"
                                    : normalized <= 100.0 ? "in_range"
                                    : "excessive";

                            return SupplementDto.DetailResponse.IngredientDetail.builder()
                                    .id(ing.getId())
                                    .name(ing.getName())
                                    .amount(amount + unit) // 예: "25.0μg"
                                    .status(status)
                                    .visualization(
                                            SupplementDto.DetailResponse.IngredientDetail.Visualization.builder()
                                                    .normalizedAmountPercent(Math.round(normalized * 10) / 10.0)
                                                    .recommendedStartPercent(Math.round(recommendedStart * 10) / 10.0)
                                                    .recommendedEndPercent(100.0)
                                                    .build()
                                    )
                                    .build();
                        })
                        .toList();

        return SupplementDto.DetailResponse.builder()
                .supplementId(supplement.getId())
                .brandId(supplement.getBrand().getId())
                .ingredients(ingredients)
                .build();
    }

    private final SearchLogRepository searchLogRepository;

    public Page<PopularSupplementDto> findPopularSupplements(String ageGroup, Gender gender, Pageable pageable) {
        // 1. 연령대 문자열을 숫자 범위로 변환
        Integer startAge = null;
        Integer endAge = null;

        if (!"전체".equals(ageGroup)) {
            if (ageGroup.equals("60대 이상")) {
                startAge = 60;
                endAge = 150; // 매우 넓은 범위로 설정
            } else if (ageGroup.contains("대")) {
                try {
                    int decade = Integer.parseInt(ageGroup.replace("대", ""));
                    startAge = decade;
                    endAge = decade + 9;
                } catch (NumberFormatException e) {
                    throw new CustomException(ErrorCode.AGEGROUP_NOT_FOUND);
                }
            } else {
                throw new CustomException(ErrorCode.AGEGROUP_NOT_MATCHED);
            }
        }

        // 2. Repository 호출하여 Tuple 페이지를 받음
        Page<Tuple> resultPage = searchLogRepository.findPopularSupplements(startAge, endAge,gender, pageable);

        // 3. Tuple 페이지를 DTO 페이지로 변환
        return resultPage.map(tuple -> {
            Supplement supplement = tuple.get(0, Supplement.class);
            long searchCount = tuple.get(1, Long.class);
            return PopularSupplementDto.from(supplement, searchCount);
        });
    }


    public SupplementDto.KeywordSearchSupplementBasedCursor searchSupplements(
            String keyword, Long cursor,  int size) {
        List<Object[]> rows = supplementRepository.findSupplementsByKeywordWithPopularity(keyword, cursor, size+1);

        List<SupplementDto.KeywordSearchSupplement> supplements = rows.stream()
                .limit(size) // size까지만 DTO 변환
                .map(row -> SupplementDto.KeywordSearchSupplement.builder()
                        .cursorId((Long) row[5])
                        .supplementName((String) row[1])
                        .coupangUrl((String) row[2])
                        .imageUrl((String) row[3])
                        .build())
                .toList();

        // nextCursor 계산
        Long nextCursor = null;
        if (rows.size() > size) {
            Object[] lastRow = rows.get(size); // size+1 번째 데이터
            nextCursor = ((Number) lastRow[5]).longValue(); // ✅ cursorId를 꺼내야 함
        }

        return SupplementDto.KeywordSearchSupplementBasedCursor.builder()
                .supplements(supplements)
                .nextCursor(nextCursor)
                .build();
    }


}