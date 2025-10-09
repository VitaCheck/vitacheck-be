package com.vitacheck.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendationResponseDto {

    private List<RecommendedCombination> recommendedCombinations;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedCombination {
        private String combinationName;
        /** 서비스/스키마와 일치하게 Integer 사용 */
        private List<Integer> supplementIds;
        private String reason;
    }
}
