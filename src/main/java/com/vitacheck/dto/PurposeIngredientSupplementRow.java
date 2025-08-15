package com.vitacheck.dto;

public record PurposeIngredientSupplementRow(
        Long ingredientId,
        String ingredientName,
        String purposeDesc,         // Enum의 description을 문자열로 담음
        Long supplementId,
        String supplementName,
        String supplementImageUrl
) {}
