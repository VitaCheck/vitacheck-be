package com.vitacheck.dto;

import com.vitacheck.domain.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LikedIngredientResponseDto {

    private Long ingredientId;
    private String name;
    private String effect;

    public static LikedIngredientResponseDto from(Ingredient ingredient) {
        return LikedIngredientResponseDto.builder()
                .ingredientId(ingredient.getId())
                .name(ingredient.getName())
                .effect(ingredient.getEffect())
                .build();
    }
}