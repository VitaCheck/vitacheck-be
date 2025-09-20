package com.vitacheck.product.dto;

import lombok.*;

import java.util.List;

public class PurposeResponseDTO {
    // 모든 목적을 반환하는 DTO
    @ToString   // ✅ 추가
    @Getter
    @AllArgsConstructor
    public static class AllPurposeDTO {
        private Long id;
        private String name;
    }

    // 선택한 목적에 맞는 성분 목록을 반환하는 DTO
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PurposeWithIngredientSupplement{
        private String name; // 목적 이름
        // 해당 목적에 매핑된 성분들 + 각 성분별 영양제
        private List<IngredientResponseDTO.IngredientWithSupplement> ingredients;
    }
}
