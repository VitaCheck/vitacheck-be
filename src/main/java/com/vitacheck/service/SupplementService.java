package com.vitacheck.service;

import com.querydsl.core.Tuple;
import com.vitacheck.auth.config.jwt.CustomUserDetails;
import com.vitacheck.common.enums.Gender;
import com.vitacheck.domain.Brand;
import com.vitacheck.domain.IngredientDosage;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.searchLog.SearchCategory;
import com.vitacheck.dto.*;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.repository.*;
import com.vitacheck.user.domain.User;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplementService {

    private final SupplementRepository supplementRepository;
    private final IngredientRepository ingredientRepository;
    private final SearchLogService searchLogService;
    private final SupplementLikeRepository supplementLikeRepository;
    private final IngredientDosageRepository dosageRepository;
    private final BrandRepository brandRepository;

    // ... (getSupplementDetail, getSupplementsByBrandId 등 다른 메서드는 그대로 둡니다) ...
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
                                    String amount = (i.getAmount() != null) ? i.getAmount().toString() : "";
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
        return supplementRepository.findAllByBrandId(brandId).stream()
                .map(SupplementDto.SimpleResponse::from)
                .toList();
    }


    @Transactional(readOnly = true)
    public SupplementDto.DetailResponse getSupplementDetailById(Long id) {

        Supplement supplement = supplementRepository.findByIdWithBrandAndIngredients(id)
                .orElseThrow(() -> new RuntimeException("해당 영양제를 찾을 수 없습니다."));

        Set<Long> ingredientIds = supplement.getSupplementIngredients().stream()
                .map(si -> si.getIngredient().getId())
                .collect(Collectors.toSet());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null; // ✅ User 객체를 담을 변수
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            user = userDetails.getUser();
        }

        // ✅✅✅ 핵심 수정 사항 시작 ✅✅✅
        // 4) 사용자 정보 기반으로 섭취 기준 조회
        Map<Long, IngredientDosage> dosageByIngredientId = getDosagesForUserAndIngredients(user, ingredientIds);
        // ✅✅✅ 핵심 수정 사항 끝 ✅✅✅

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
                                    .amount(amount + unit)
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

    // ✅✅✅ 이 헬퍼 메서드를 추가합니다 ✅✅✅
    private Map<Long, IngredientDosage> getDosagesForUserAndIngredients(User user, Set<Long> ingredientIds) {
        if (ingredientIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Gender gender = (user != null && user.getGender() != null) ? user.getGender() : Gender.ALL;
        int age = (user != null && user.getBirthDate() != null) ? Period.between(user.getBirthDate(), LocalDate.now()).getYears() : 25; // 기본값 25세

        List<Gender> genderOptions = List.of(gender, Gender.ALL);
        List<Long> idList = new ArrayList<>(ingredientIds);

        List<IngredientDosage> dosages = dosageRepository.findApplicableDosages(idList, genderOptions, age);

        return dosages.stream()
                .collect(Collectors.toMap(
                        dosage -> dosage.getIngredient().getId(),
                        dosage -> dosage,
                        // 성별이 정확히 일치하는 것을 우선으로 선택
                        (dosage1, dosage2) -> dosage1.getGender() != Gender.ALL ? dosage1 : dosage2
                ));
    }


    private final SearchLogRepository searchLogRepository;

    public Page<PopularSupplementDto> findPopularSupplements(String ageGroup, Gender gender, Pageable pageable) {
        Integer startAge = null;
        Integer endAge = null;

        if (!"전체".equals(ageGroup)) {
            if (ageGroup.equals("60대 이상")) {
                startAge = 60;
                endAge = 150;
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

        Page<Tuple> resultPage = searchLogRepository.findPopularSupplements(startAge, endAge,gender, pageable);

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
                .limit(size)
                .map(row -> SupplementDto.KeywordSearchSupplement.builder()
                        .cursorId((Long) row[5])
                        .supplementName((String) row[1])
                        .coupangUrl((String) row[2])
                        .imageUrl((String) row[3])
                        .build())
                .toList();

        Long nextCursor = null;
        if (rows.size() > size) {
            Object[] lastRow = rows.get(size);
            nextCursor = ((Number) lastRow[5]).longValue();
        }

        return SupplementDto.KeywordSearchSupplementBasedCursor.builder()
                .supplements(supplements)
                .nextCursor(nextCursor)
                .build();
    }
}