package com.vitacheck.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.vitacheck.domain.user.Gender;
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
        private String caution; // 부작용 및 주의사항
        private Gender gender;
        private Integer age;
        private Double upperLimit;     // 상한
        private Double recommendedDosage;     // 하한
        private String unit;           // 단위
        private List<SubIngredient> subIngredients = new ArrayList<>(); // 대체 식품
        private List<IngredientSupplement> supplements; // 영양제
        private String DosageErrorCode;  // 상한, 권장량 오류
        private String FoodErrorCode;    // 대체 식품 오류
        private String SupplementErrorCode;  // 관련 영양제 오류
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class IngredientSupplement{
        private Long id;
        private String name;
        private String coupangUrl;
        private String imageUrl;
    }
    

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SubIngredient {
        private String name;
        private String imageOrEmoji;  // 이미지 URL 또는 이모지 표현
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class IngredientName {
        private Long id;
        private String name;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class IngredientWithSupplement {
        private Long ingredientId;                 // 성분 ID
        private String ingredientName;             // 성분 이름
        private List<IngredientSupplement> supplements; // 관련 영양제
    }

}


