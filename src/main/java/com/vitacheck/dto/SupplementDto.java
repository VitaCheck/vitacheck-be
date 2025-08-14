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
        private Double amount;
        private String unit;

        // SupplementIngredient 엔티티를 이 DTO로 변환
        public static IngredientInfo from(SupplementIngredient si) {
            return IngredientInfo.builder()
                    .ingredientName(si.getIngredient().getName())
                    .amount(si.getAmount())
                    // si.getUnit() 대신 si.getIngredient().getUnit()으로 수정
                    .unit(si.getIngredient().getUnit())
                    .build();
        }
    }

    // 특정 브랜드의 다른 영양제 목록 반환 시 사용할 간단한 DTO입니당 by 나영
    @Getter
    @Builder
    public static class SimpleResponse {
        private Long id;
        private String name;
        private String imageUrl;

        public static SimpleResponse from(Supplement supplement) {
            return SimpleResponse.builder()
                    .id(supplement.getId())
                    .name(supplement.getName())
                    .imageUrl(supplement.getImageUrl())
                    .build();
        }
    }

    // 특정 영양제의 상세 정보 반환 시 사용될 DTO 입니다 by 나영
    @Getter
    @Builder
    public static class DetailResponse {
        private Long supplementId;
        private Long brandId;
        private List<IngredientDetail> ingredients;

        public static DetailResponse from(Supplement supplement, List<IngredientDetail> ingredientDetails) {
            return DetailResponse.builder()
                    .supplementId(supplement.getId())
                    .brandId(supplement.getBrand().getId())
                    .ingredients(ingredientDetails)
                    .build();
        }

        @Getter
        @Builder
        public static class IngredientDetail {
            private String name;
            private Long id;
            private String amount; // 단위 포함 문자열 (ex: 20ug)
            private String status; // deficient, in_range, excessive
            private Visualization visualization;

            @Getter
            @Builder
            public static class Visualization {
                private double normalizedAmountPercent;
                private double recommendedStartPercent; // 항상 30.0 임
                private double recommendedEndPercent; // 항상 70.0 임
            }
        }
    }
}