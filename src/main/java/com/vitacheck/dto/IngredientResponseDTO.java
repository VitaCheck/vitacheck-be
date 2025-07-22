package com.vitacheck.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class IngredientResponseDTO {
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class IngredientDetails{
        private Long id;
        private String name;
        private String description;    // 설명
        private String effect;         // 효능
        private String caution;        // 부작용 및 주의사
        private Double upperLimit;     // 상한
        private Double lowerLimit;     // 하한
        private String unit;           // 단위
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class IngredientSupplement{
        private Long id;
        private String name;
        private List<SubIngredient> subIngredients = new ArrayList<>();
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class IngredientFood{
        private Long id;
        private String name;
        private List<SubIngredient> subIngredients = new ArrayList<>();
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SubIngredient {
        private String name;
        private String imageOrEmoji;  // 이미지 URL 또는 이모지 표현
    }


}


