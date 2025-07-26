package com.vitacheck.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vitacheck.domain.Supplement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.vitacheck.domain.QBrand.brand;
import static com.vitacheck.domain.QIngredient.ingredient;
import static com.vitacheck.domain.QSupplement.supplement;
import static com.vitacheck.domain.mapping.QSupplementIngredient.supplementIngredient;

@Repository
@RequiredArgsConstructor
public class SupplementRepositoryImpl implements SupplementRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Supplement> search(String keyword, String brandName, String ingredientName, Pageable pageable) {
        // 데이터 조회를 위한 기본 쿼리
        List<Supplement> content = queryFactory
                .selectFrom(supplement)
                .leftJoin(supplement.brand, brand).fetchJoin()
                .leftJoin(supplement.supplementIngredients, supplementIngredient).fetchJoin()
                .leftJoin(supplementIngredient.ingredient, ingredient).fetchJoin()
                .where(
                        unifiedSearch(keyword),
                        hasBrandName(brandName),
                        hasIngredientName(ingredientName)
                )
                .distinct()
                .offset(pageable.getOffset()) // 페이지네이션 적용
                .limit(pageable.getPageSize())  // 페이지네이션 적용
                .fetch();

        // 전체 카운트 조회를 위한 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(supplement.countDistinct())
                .from(supplement)
                .leftJoin(supplement.brand, brand)
                .leftJoin(supplement.supplementIngredients, supplementIngredient)
                .leftJoin(supplementIngredient.ingredient, ingredient)
                .where(
                        unifiedSearch(keyword),
                        hasBrandName(brandName),
                        hasIngredientName(ingredientName)
                );

        return new PageImpl<>(content, pageable, countQuery.fetchOne());
    }

    private BooleanExpression unifiedSearch(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return supplement.name.containsIgnoreCase(keyword)
                .or(brand.name.containsIgnoreCase(keyword))
                .or(ingredient.name.containsIgnoreCase(keyword));
    }

    private BooleanExpression hasBrandName(String brandName) {
        return StringUtils.hasText(brandName) ? brand.name.eq(brandName) : null;
    }

    private BooleanExpression hasIngredientName(String ingredientName) {
        return StringUtils.hasText(ingredientName) ? ingredient.name.eq(ingredientName) : null;
    }
}