package com.vitacheck.dto;

import com.vitacheck.domain.Combination.Combination;
import com.vitacheck.domain.Combination.RecommandType;
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
    @AllArgsConstructor
    public static class AnalysisResponse {

        private List<IngredientAnalysisResultDto> ingredientResults;
        // 분석된 개별 성분 결과
        @Getter
        @Builder
        public static class IngredientAnalysisResultDto {
            private String ingredientName; // 성분명 (예: 비타민 C)
            private Integer totalAmount;       // 총 섭취량
            private String unit;           // 단위 (mg, IU 등)
            private Integer recommendedAmount; // 권장량
            private Integer upperAmount;       // 상한량
            private boolean isOverRecommended; // 권장량 초과 여부 (true/false)
            private double dosageRatio;        // 섭취 비율 (0.0 ~ 1.0)
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
