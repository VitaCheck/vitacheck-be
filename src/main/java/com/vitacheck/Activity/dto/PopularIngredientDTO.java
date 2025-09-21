package com.vitacheck.Activity.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class PopularIngredientDTO {
    private Long ingredientId;
    private String ingredientName;
    private long searchCount;

    @QueryProjection // 👈 이 어노테이션이 필수입니다!
    public PopularIngredientDTO(Long ingredientId, String ingredientName, long searchCount) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.searchCount = searchCount;
    }
}