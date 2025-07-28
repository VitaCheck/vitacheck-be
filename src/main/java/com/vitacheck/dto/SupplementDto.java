package com.vitacheck.dto;

import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.mapping.SupplementIngredient;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

public class SupplementDto {

    @Getter
    @Builder
    public static class SearchResponse {
        // 영양제 기본 정보
        private Long supplementId;
        private String supplementName;
        private String imageUrl;
        private Integer price;
        private String description;
        private String method;
        private String caution;

        // 연관 정보
        private String brandName;
        private List<IngredientInfo> ingredients;

        // Supplement 엔티티를 이 DTO로 변환하는 정적 팩토리 메소드
        public static SearchResponse from(Supplement supplement) {
            return SearchResponse.builder()
                    .supplementId(supplement.getId())
                    .supplementName(supplement.getName())
                    .imageUrl(supplement.getImageUrl())
                    .price(supplement.getPrice())
                    .description(supplement.getDescription())
                    .method(supplement.getMethod())
                    .caution(supplement.getCaution())
                    .brandName(supplement.getBrand().getName())
                    .ingredients(supplement.getSupplementIngredients().stream()
                            .map(IngredientInfo::from)
                            .collect(Collectors.toList()))
                    .build();
        }
    }



    @Getter
    @Builder
    public static class IngredientInfo {
        private String ingredientName;
        private Integer amount;
        private String unit;

        // SupplementIngredient 엔티티를 이 DTO로 변환
        public static IngredientInfo from(SupplementIngredient si) {
            return IngredientInfo.builder()
                    .ingredientName(si.getIngredient().getName())
                    .amount(si.getAmount())
                    .unit(si.getUnit())
                    .build();
        }
    }
}