package com.vitacheck.dto;


import com.vitacheck.product.domain.Ingredient.Ingredient;
import com.vitacheck.product.dto.SupplementResponseDTO;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

public class SearchDto {

    @Getter
    @Builder
    public static class UnifiedSearchResponse {
        private List<IngredientInfo> matchedIngredients; // ✅ 매칭된 성분 정보 (없으면 null)
        private Page<SupplementResponseDTO.SearchResponse> supplements; // ✅ 기존 영양제 검색 결과
    }

    @Getter
    @Builder
    public static class IngredientInfo {
        private Long ingredientId;
        private String name;
        private String description;
        private String effect;

        // Ingredient 엔티티를 DTO로 변환
        public static IngredientInfo from(Ingredient ingredient) {
            return IngredientInfo.builder()
                    .ingredientId(ingredient.getId())
                    .name(ingredient.getName())
                    .description(ingredient.getDescription())
                    .effect(ingredient.getEffect())
                    .build();
        }
    }
}