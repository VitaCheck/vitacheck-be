package com.vitacheck.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.vitacheck.dto.QPopularIngredientDto is a Querydsl Projection type for PopularIngredientDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QPopularIngredientDto extends ConstructorExpression<PopularIngredientDto> {

    private static final long serialVersionUID = -1515709737L;

    public QPopularIngredientDto(com.querydsl.core.types.Expression<Long> ingredientId, com.querydsl.core.types.Expression<String> ingredientName, com.querydsl.core.types.Expression<Long> searchCount) {
        super(PopularIngredientDto.class, new Class<?>[]{long.class, String.class, long.class}, ingredientId, ingredientName, searchCount);
    }

}

