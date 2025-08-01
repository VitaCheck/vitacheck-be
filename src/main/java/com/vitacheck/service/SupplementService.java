package com.vitacheck.service;

import com.vitacheck.domain.Ingredient;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.mapping.IngredientCategory;
import com.vitacheck.domain.mapping.SupplementIngredient;
import com.vitacheck.domain.purposes.AllPurpose;
import com.vitacheck.domain.purposes.PurposeCategory;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.*;
import com.vitacheck.repository.IngredientRepository;
import com.vitacheck.repository.LikeRepository;
import com.vitacheck.repository.PurposeCategoryRepository;
import com.vitacheck.repository.SupplementRepository;
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
    private final LikeRepository likeRepository;

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

        boolean liked = likeRepository.existsByUserIdAndSupplementId(userId, supplementId);

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

}