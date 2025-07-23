package com.vitacheck.dto;

import com.vitacheck.domain.combination.Combination;
import com.vitacheck.domain.combination.RecommandType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class CombinationDTO {
    @Getter
    @Setter
    public static class AnalysisRequest {
        private List<Long> supplementIds;
    }

    @Getter
    @Builder
    @AllArgsConstructor // ✅ new AnalysisResponse(ingredientResults)를 위해 생성자 추가
    public static class AnalysisResponse {

        private List<IngredientAnalysisResultDto> ingredientResults;

        @Getter
        @Builder
        public static class IngredientAnalysisResultDto {
            // --- 기존 필드 ---
            private String ingredientName;
            private Integer totalAmount;
            private String unit;
            private Double recommendedAmount;
            private Double upperAmount;

            // ✅ 서비스 로직에서 계산한 필드 추가
            private boolean isOverRecommended; // 권장량 초과 여부
            private double dosageRatio;        // 섭취 비율
        }
    }

    @Getter
    @AllArgsConstructor
    public static class RecommendCombinationResponse {
        private List<RecommendResultDTO> goodCombinations;
        private List<RecommendResultDTO> cautionCombinations;
    }

    @Getter
    @Builder
    public static class RecommendResultDTO {
        private Long id;
        private RecommandType type;
        private String name;
        private String description;
        private Integer displayRank;

        public static RecommendResultDTO from(Combination combination) {
            return RecommendResultDTO.builder()
                    .id(combination.getId())
                    .type(combination.getType())
                    .name(combination.getName())
                    .description(combination.getDescription())
                    .displayRank(combination.getDisplayRank())
                    .build();
        }
    }
}