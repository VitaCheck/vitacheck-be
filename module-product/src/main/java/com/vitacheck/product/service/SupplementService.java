package com.vitacheck.product.service;

import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.common.enums.Gender;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.product.domain.Ingredient.IngredientDosage;
import com.vitacheck.product.domain.Supplement.Brand;
import com.vitacheck.product.domain.Supplement.Supplement;
import com.vitacheck.product.dto.SupplementResponseDTO;
import com.vitacheck.product.repository.IngredientDosageRepository;
import com.vitacheck.product.repository.IngredientRepository;
import com.vitacheck.product.repository.SupplementRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;


import java.util.*;
        import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplementService {

    private final SupplementRepository supplementRepository;
    private final IngredientDosageRepository dosageRepository;

//    public SupplementService(SupplementRepository supplementRepository) {
//        this.supplementRepository = supplementRepository;
//    }


//    private List<SearchDTO.IngredientInfo> findMatchedIngredients(String keyword, String ingredientName) {
//        // 1. 실제 검색에 사용할 검색어를 정합니다. ingredientName이 우선순위를 가집니다.
//        String finalSearchTerm = StringUtils.hasText(ingredientName) ? ingredientName : keyword;
//
//        // 2. 검색어가 존재하는 경우, 이름을 포함하는 모든 성분을 검색합니다.
//        if (StringUtils.hasText(finalSearchTerm)) {
//            return ingredientRepository.findByNameContainingIgnoreCase(finalSearchTerm).stream()
//                    .map(SearchDto.IngredientInfo::from)
//                    .collect(Collectors.toList());
//        }
//
//        // 3. 검색어가 없으면 빈 리스트를 반환합니다.
//        return Collections.emptyList();
//    }



    @Transactional(readOnly = true)
    public SupplementResponseDTO.SupplementDetail getSupplementDetail(Long supplementId) {
        Supplement supplement = supplementRepository.findById(supplementId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUPPLEMENT_NOT_FOUND));

        Brand brand = supplement.getBrand();

//        boolean liked = (userId != null) && supplementLikeRepository.existsByUserIdAndSupplementId(userId, supplementId);

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
//            // 🔹 클릭 로그 저장 (미로그인)
//            searchLogService.logClick(null, supplement.getName(), SearchCategory.SUPPLEMENT, null,null);
//
//        } else {
//            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//            User user = userDetails.getUser();
//            LocalDate birthDate = user.getBirthDate();
//            int age = Period.between(birthDate, LocalDate.now()).getYears();
//
//            // 🔹 클릭 로그 저장 (로그인)
//            searchLogService.logClick(user.getId(), supplement.getName(), SearchCategory.SUPPLEMENT, age, user.getGender());
//        }

        return SupplementResponseDTO.SupplementDetail.builder()
                .supplementId(supplement.getId())
                .brandId(brand != null ? brand.getId() : null)
                .brandName(supplement.getBrand().getName())
                .brandImageUrl(supplement.getBrand().getImageUrl())
                .supplementName(supplement.getName())
                .supplementImageUrl(supplement.getImageUrl())
//                .liked(liked)
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

                                    return new SupplementResponseDTO.SupplementDetail.IngredientDto(
                                            i.getIngredient().getName(),
                                            amount + unit
                                    );
                                })
                                .toList())
                .build();
    }



    public List<SupplementResponseDTO.SimpleResponse> getSupplementsByBrandId(Long brandId) {
        // 이 메소드는 변경 없음
        return supplementRepository.findAllByBrandId(brandId).stream()
                .map(SupplementResponseDTO.SimpleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplementResponseDTO.DetailResponse getSupplementDetailById(Long id) {

        // 1) 영양제 + 성분 한 번에 조회
        Supplement supplement = supplementRepository.findByIdWithBrandAndIngredients(id)
                .orElseThrow(() -> new RuntimeException("해당 영양제를 찾을 수 없습니다."));

        // 2) 성분 ID 수집
        Set<Long> ingredientIds = supplement.getSupplementIngredients().stream()
                .map(si -> si.getIngredient().getId())
                .collect(Collectors.toSet());

        // 3) 사용자 성별/나이

        Gender gender = Gender.ALL;
        Integer age = null;
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
//            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
//            User user = userDetails.getUser();
//            gender = (user.getGender() != null) ? user.getGender() : Gender.ALL;
//
//            if (user.getBirthDate() != null) {
//                age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();
//            }
//
//            // 클릭 로그(로그인)
//            searchLogService.logClick(user.getId(), supplement.getName(), SearchCategory.SUPPLEMENT, age, gender);
//        } else {
//            // 클릭 로그(비로그인)
//            searchLogService.logClick(null, supplement.getName(), SearchCategory.SUPPLEMENT, null, null);
//        }

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
        List<SupplementResponseDTO.DetailResponse.IngredientDetail> ingredients =
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

                            return SupplementResponseDTO.DetailResponse.IngredientDetail.builder()
                                    .id(ing.getId())
                                    .name(ing.getName())
                                    .amount(amount + unit) // 예: "25.0μg"
                                    .status(status)
                                    .visualization(
                                            SupplementResponseDTO.DetailResponse.IngredientDetail.Visualization.builder()
                                                    .normalizedAmountPercent(Math.round(normalized * 10) / 10.0)
                                                    .recommendedStartPercent(Math.round(recommendedStart * 10) / 10.0)
                                                    .recommendedEndPercent(100.0)
                                                    .build()
                                    )
                                    .build();
                        })
                        .toList();

        return SupplementResponseDTO.DetailResponse.builder()
                .supplementId(supplement.getId())
                .brandId(supplement.getBrand().getId())
                .ingredients(ingredients)
                .build();
    }


    public SupplementResponseDTO.KeywordSearchSupplementBasedCursor searchSupplements(
            String keyword, Long cursor,  int size) {
        List<Object[]> rows = supplementRepository.findSupplementsByKeywordWithPopularity(keyword, cursor, size+1);

        List<SupplementResponseDTO.KeywordSearchSupplement> supplements = rows.stream()
                .limit(size) // size까지만 DTO 변환
                .map(row -> SupplementResponseDTO.KeywordSearchSupplement.builder()
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

        return SupplementResponseDTO.KeywordSearchSupplementBasedCursor.builder()
                .supplements(supplements)
                .nextCursor(nextCursor)
                .build();
    }


}
