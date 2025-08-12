package com.vitacheck.service;

import com.vitacheck.domain.Ingredient;
import com.vitacheck.domain.IngredientDosage;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.mapping.IngredientCategory;
import com.vitacheck.domain.mapping.SupplementIngredient;
import com.vitacheck.domain.purposes.AllPurpose;
import com.vitacheck.domain.purposes.PurposeCategory;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.*;
import com.vitacheck.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
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

    public SearchDto.UnifiedSearchResponse search(User user, String keyword, String brandName, String ingredientName, Pageable pageable) {

        List<SearchDto.IngredientInfo> matchedIngredients = findMatchedIngredients(keyword, ingredientName);

        Page<Supplement> supplementsPage = supplementRepository.search(keyword, brandName, ingredientName, pageable);
        Page<SupplementDto.SearchResponse> supplementDtos = supplementsPage.map(SupplementDto.SearchResponse::from);

        if (user != null) {
            searchLogService.logSearch(user, keyword, brandName, ingredientName);
            if (StringUtils.hasText(ingredientName)) {
                statisticsService.updateIngredientSearchStats(user, ingredientName);
            }
        }

        return SearchDto.UnifiedSearchResponse.builder()
                .matchedIngredients(matchedIngredients)
                .supplements(supplementDtos)
                .build();
    }


    private List<SearchDto.IngredientInfo> findMatchedIngredients(String keyword, String ingredientName) {
        // ingredientName 파라미터가 있으면 정확히 일치하는 1개만 찾음
        if (StringUtils.hasText(ingredientName)) {
            return ingredientRepository.findByName(ingredientName)
                    .stream()
                    .map(SearchDto.IngredientInfo::from)
                    .collect(Collectors.toList());
        }
        // ingredientName이 없고 keyword만 있으면 이름에 포함되는 모든 성분을 찾음
        if (StringUtils.hasText(keyword)) {
            return ingredientRepository.findByNameContainingIgnoreCase(keyword).stream()
                    .map(SearchDto.IngredientInfo::from)
                    .collect(Collectors.toList());
        }
        // 둘 다 없으면 빈 리스트 반환
        return Collections.emptyList();
    }

    @Transactional(readOnly = true)
    public Map<String, SupplementByPurposeResponse> getSupplementsByPurposes(SupplementPurposeRequest request) {
        List<AllPurpose> allPurposes = request.getPurposeNames().stream()
                .map(AllPurpose::valueOf)
                .toList();

        // N+1 지점: 목적 -> 성분, 성분 -> 영양제 모두 Lazy 로딩
        // QueryDSL로 fetch join 처리된 메서드로 교체
        // List<PurposeCategory> categories = purposeCategoryRepository.findAllByNameIn(allPurposes);
        List<PurposeCategory> categories = purposeCategoryRepository.findAllWithIngredientAndSupplementByNameIn(allPurposes);

        // 성분 -> 목적 리스트 매핑
        Map<Ingredient, Set<String>> ingredientToPurposes = new HashMap<>();

        for (PurposeCategory category : categories) {
            String purposeDesc = category.getName().getDescription();
            // category.getIngredientCategories() 가 Lazy 일 경우 루프마다 쿼리 발생
            for (IngredientCategory ic : category.getIngredientCategories()) {
                // ic.getIngredient() 도 Lazy 로딩 시마다 쿼리 발생
                Ingredient ingredient = ic.getIngredient();
                ingredientToPurposes
                        .computeIfAbsent(ingredient, k -> new HashSet<>())
                        .add(purposeDesc);
            }
        }

        Map<String, SupplementByPurposeResponse> result = new HashMap<>();

        for (Map.Entry<Ingredient, Set<String>> entry : ingredientToPurposes.entrySet()) {
            Ingredient ingredient = entry.getKey();
            Set<String> purposes = entry.getValue();

            // ingredient.getSupplementIngredients() 도 Lazy
            // 각 SupplementIngredient → Supplement 도 추가 쿼리 발생 가능
            List<List<String>> supplementInfo = ingredient.getSupplementIngredients().stream()
                    .map(SupplementIngredient::getSupplement)
                    .map(supplement -> List.of(supplement.getName(), supplement.getImageUrl()))
                    .toList();

            result.put(ingredient.getName(),
                    SupplementByPurposeResponse.builder()
                            .purposes(new ArrayList<>(purposes))
                            .supplements(supplementInfo)
                            .build()
            );
        }

        return result;
    }

    @Transactional(readOnly = true)
    public SupplementDetailResponseDto getSupplementDetail(Long supplementId, Long userId) {
        Supplement supplement = supplementRepository.findById(supplementId)
                .orElseThrow(() -> new IllegalArgumentException("해당 영양제를 찾을 수 없습니다."));

        boolean liked = false;
        if (userId != null) {
            liked = supplementLikeRepository.existsByUserIdAndSupplementId(userId, supplementId);
        }

        return SupplementDetailResponseDto.builder()
                .supplementId(supplement.getId())
                .brandName(supplement.getBrand().getName())
                .brandImageUrl(supplement.getBrand().getImageUrl())
                .supplementName(supplement.getName())
                .supplementImageUrl(supplement.getImageUrl())
                .liked(liked)
                .coupangLink(supplement.getCoupangUrl())
                .intakeTime(supplement.getMethod())
                .ingredients(
                        supplement.getSupplementIngredients().stream()
                                .map(i -> new SupplementDetailResponseDto.IngredientDto(
                                        i.getIngredient().getName(),
                                        (i.getAmount() != null ? i.getAmount() : "") + i.getUnit()
                                ))
                                .toList())
                .build();
    }

    public List<SupplementDto.SimpleResponse> getSupplementsByBrandId(Long brandId) {
        return supplementRepository.findAllByBrandId(brandId).stream()
                .map(SupplementDto.SimpleResponse::from)
                .toList();
    }

    public SupplementDto.DetailResponse getSupplementDetailById(Long id) {
        Supplement supplement = supplementRepository.findByIdWithIngredients(id)
                .orElseThrow(() -> new RuntimeException("해당 영양제를 찾을 수 없습니다."));

        List<SupplementDto.DetailResponse.IngredientDetail> ingredients =
                supplement.getSupplementIngredients().stream()
                        .map(si -> {
                            Ingredient ingredient = si.getIngredient();
                            IngredientDosage dosage = dosageRepository.findGeneralDosageByIngredientId(ingredient.getId())
                                    .orElseThrow(() -> new RuntimeException("기준 정보 없음: " + ingredient.getName()));

                            double amount = si.getAmount();
                            String unit = si.getUnit();
                            double ul = dosage.getUpperLimit();

                            double percent = (amount / ul) * 100.0;
                            percent = Math.min(percent, 999); // 너무 큰 값 제한

                            String status = percent < 30.0 ? "deficient"
                                    : percent <= 70.0 ? "in_range"
                                    : "excessive";

                            return SupplementDto.DetailResponse.IngredientDetail.builder()
                                    .id(ingredient.getId())
                                    .name(ingredient.getName())
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
                .ingredients(ingredients)
                .build();
    }
}