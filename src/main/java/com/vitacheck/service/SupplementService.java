package com.vitacheck.service;

import com.vitacheck.domain.Ingredient;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.mapping.IngredientCategory;
import com.vitacheck.domain.mapping.SupplementIngredient;
import com.vitacheck.domain.purposes.AllPurpose;
import com.vitacheck.domain.purposes.PurposeCategory;
import com.vitacheck.dto.SupplementByPurposeResponse;
import com.vitacheck.dto.SupplementDto;
import com.vitacheck.dto.SupplementPurposeRequest;
import com.vitacheck.repository.PurposeCategoryRepository;
import com.vitacheck.repository.SupplementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplementService {

    private final SupplementRepository supplementRepository;
    private final PurposeCategoryRepository purposeCategoryRepository;

    public List<SupplementDto.SearchResponse> search(String keyword, String brandName, String ingredientName) {
        List<Supplement> supplements = supplementRepository.search(keyword, brandName, ingredientName);

        return supplements.stream()
                .map(SupplementDto.SearchResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, SupplementByPurposeResponse> getSupplementsByPurposes(SupplementPurposeRequest request) {
        List<AllPurpose> allPurposes = request.getPurposeNames().stream()
                .map(AllPurpose::valueOf)
                .toList();

        List<PurposeCategory> categories = purposeCategoryRepository.findAllByNameIn(allPurposes);

        // 성분 -> 목적 리스트 매핑
        Map<Ingredient, Set<String>> ingredientToPurposes = new HashMap<>();

        for (PurposeCategory category : categories) {
            String purposeDesc = category.getName().getDescription();
            for (IngredientCategory ic : category.getIngredientCategories()) {
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
}