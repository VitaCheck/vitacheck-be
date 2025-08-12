package com.vitacheck.service;

import com.vitacheck.domain.Ingredient;
import com.vitacheck.domain.IngredientDosage;
import com.vitacheck.domain.Supplement;
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

        // 1. 목적(Purpose)으로 PurposeCategory 엔티티를 조회합니다.
        List<PurposeCategory> categories = purposeCategoryRepository.findAllByNameIn(allPurposes);

        // 결과를 담을 Map을 생성합니다.
        Map<String, SupplementByPurposeResponse> result = new HashMap<>();

        // 2. 각 PurposeCategory를 순회합니다.
        for (PurposeCategory category : categories) {

            // 3. category.getIngredients()를 통해 직접 성분(Ingredient) 목록에 접근합니다.
            for (Ingredient ingredient : category.getIngredients()) {

                // 4. 각 성분에 연결된 영양제 정보를 가져옵니다.
                List<List<String>> supplementInfo = ingredient.getSupplementIngredients().stream()
                        .map(si -> si.getSupplement())
                        .map(supplement -> List.of(supplement.getName(), supplement.getImageUrl()))
                        .toList();

                // 5. 목적(Purpose) 목록을 가져옵니다.
                List<String> purposes = ingredient.getPurposeCategories().stream()
                        .map(pc -> pc.getName().getDescription())
                        .toList();

                // 6. 최종 결과 Map에 담습니다.
                result.put(ingredient.getName(),
                        SupplementByPurposeResponse.builder()
                                .purposes(purposes)
                                .supplements(supplementInfo)
                                .build());
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public SupplementDetailResponseDto getSupplementDetail(Long supplementId, Long userId) {
        Supplement supplement = supplementRepository.findById(supplementId)
                .orElseThrow(() -> new IllegalArgumentException("해당 영양제를 찾을 수 없습니다."));

        boolean liked = (userId != null) && supplementLikeRepository.existsByUserIdAndSupplementId(userId, supplementId);

        // 1. 영양제에 포함된 모든 성분의 ID 목록을 추출합니다.
        List<Long> ingredientIds = supplement.getSupplementIngredients().stream()
                .map(si -> si.getIngredient().getId())
                .toList();

        // 2. 성분 ID 목록으로 모든 Dosage 정보를 한 번의 쿼리로 가져와 Map으로 만듭니다.
        Map<Long, IngredientDosage> dosageMap = dosageRepository.findGeneralDosageByIngredientIdIn(ingredientIds).stream()
                .collect(Collectors.toMap(dosage -> dosage.getIngredient().getId(), Function.identity()));

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
                                .map(si -> {
                                    // 3. Map에서 해당 성분의 Dosage 정보를 찾아 단위를 사용합니다.
                                    IngredientDosage dosage = dosageMap.get(si.getIngredient().getId());
                                    String unit = (dosage != null) ? dosage.getUnit() : "";
                                    String amount = (si.getAmount() != null) ? si.getAmount().toString() : "";

                                    return new SupplementDetailResponseDto.IngredientDto(
                                            si.getIngredient().getName(),
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
                            // dosage 객체에서 unit 정보를 가져오도록 수정
                            String unit = dosage.getUnit();
                            double ul = dosage.getUpperLimit();

                            double percent = (ul > 0) ? (amount / ul) * 100.0 : 0.0;
                            percent = Math.min(percent, 999);

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