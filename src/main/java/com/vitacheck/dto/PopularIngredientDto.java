package com.vitacheck.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class PopularIngredientDto {
    private String ingredientName;
    private long searchCount;

    @QueryProjection // 👈 이 어노테이션이 필수입니다!
    public PopularIngredientDto(String ingredientName, long searchCount) {
        this.ingredientName = ingredientName;
        this.searchCount = searchCount;
    }
}