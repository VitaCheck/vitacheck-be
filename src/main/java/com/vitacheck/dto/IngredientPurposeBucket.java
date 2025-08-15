package com.vitacheck.dto;

import lombok.Builder;

@Builder
public record IngredientPurposeBucket(
        String ingredientName,
        SupplementByPurposeResponse data
) {}
