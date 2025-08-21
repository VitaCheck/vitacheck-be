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

    // 👇👇👇 [수정] getSupplementDetailById 메소드를 원래 로직으로 되돌립니다. 👇👇👇
    public SupplementDto.DetailResponse getSupplementDetailById(Long id) {
        // 1) 상세 엔티티는 brand/ingredients까지 한방에 가져오는 메서드로 (아래 3-2에서 추가)
        Supplement supplement = supplementRepository.findByIdWithBrandAndIngredients(id)
                .orElseThrow(() -> new RuntimeException("해당 영양제를 찾을 수 없습니다."));

        // 2) 성분 ID 모으기
        Set<Long> ingredientIds = supplement.getSupplementIngredients().stream()
                .map(si -> si.getIngredient().getId())
                .collect(Collectors.toSet());

        // 3) 권장량/UL 벌크 로딩 후 맵으로
        Map<Long, IngredientDosage> dosageByIngredientId = dosageRepository
                .findGeneralDosagesByIngredientIds(ingredientIds).stream()
                .collect(Collectors.toMap(d -> d.getIngredient().getId(), d -> d));

        // 4) 매핑 시 DB 추가 접근 없음
        List<SupplementDto.DetailResponse.IngredientDetail> ingredients =
                supplement.getSupplementIngredients().stream()
                        .map(si -> {
                            var ing = si.getIngredient();
                            var dosage = dosageByIngredientId.get(ing.getId());

                            double amount = si.getAmount() != null ? si.getAmount() : 0.0;
                            String unit = ing.getUnit() != null ? ing.getUnit() : "";

                            double ul = (dosage != null && dosage.getUpperLimit() != null) ? dosage.getUpperLimit() : 0.0;
                            double percent = (ul > 0) ? (amount / ul) * 100.0 : 0.0;
                            percent = Math.min(percent, 999.0);

                            String status = percent < 30.0 ? "deficient"
                                    : percent <= 70.0 ? "in_range"
                                    : "excessive";

                            return SupplementDto.DetailResponse.IngredientDetail.builder()
                                    .id(ing.getId())
                                    .name(ing.getName())
                                    .amount(amount + unit)
                                    .status(status)
                                    .visualization(SupplementDto.DetailResponse.IngredientDetail.Visualization.builder()
                                            .normalizedAmountPercent(Math.round(percent * 10) / 10.0)
                                            .recommendedStartPercent(30.0)
                                            .recommendedEndPercent(70.0)
                                            .build())
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