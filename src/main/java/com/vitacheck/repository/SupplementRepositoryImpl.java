package com.vitacheck.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vitacheck.domain.Supplement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.vitacheck.domain.QSupplement.supplement;
import static com.vitacheck.domain.QSupplementIngredient.supplementIngredient;
import static com.vitacheck.domain.QBrand.brand;

@Repository
@RequiredArgsConstructor
public class SupplementRepositoryImpl implements SupplementRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Supplement> search(String keyword, String brandName, String ingredientName) {
        return queryFactory
                .selectFrom(supplement)
                .leftJoin(supplement.brand, brand).fetchJoin()
                .leftJoin(supplement.supplementIngredients, supplementIngredient).fetchJoin()
                .leftJoin(supplementIngredient.ingredient).fetchJoin()
                .where(
                        nameContains(keyword),
                        hasBrandName(brandName),
                        hasIngredientName(ingredientName)
                )
                .distinct()
                .fetch();
    }

    private BooleanExpression nameContains(String keyword) {
        return StringUtils.hasText(keyword) ? supplement.name.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression hasBrandName(String brandName) {
        return StringUtils.hasText(brandName) ? supplement.brand.name.eq(brandName) : null;
    }

    private BooleanExpression hasIngredientName(String ingredientName) {
        return StringUtils.hasText(ingredientName) ? supplementIngredient.ingredient.name.eq(ingredientName) : null;
    }
}