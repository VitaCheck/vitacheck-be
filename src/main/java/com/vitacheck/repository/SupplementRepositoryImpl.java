package com.vitacheck.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.user.User;
import com.vitacheck.util.DateUtils;
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
import static com.vitacheck.domain.QAgeGroupSupplementStats.ageGroupSupplementStats;

@Repository
@RequiredArgsConstructor
public class SupplementRepositoryImpl implements SupplementRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Supplement> search(User user, String keyword, String brandName, String ingredientName, Pageable pageable) {
        Expression<Long> popularityScore;

        if (user!=null) {
            String ageGroup = DateUtils.calculateAgeGroup(user.getBirthDate());

            popularityScore = JPAExpressions.select(ageGroupSupplementStats.clickCount.coalesce(0L))
                    .from(ageGroupSupplementStats)
                    .where(ageGroupSupplementStats.supplement.eq(supplement)
                            .and(ageGroupSupplementStats.age.eq(ageGroup)));
        }else {
            popularityScore = JPAExpressions.select(ageGroupSupplementStats.clickCount.sum().coalesce(0L))
                    .from(ageGroupSupplementStats)
                    .where(ageGroupSupplementStats.supplement.eq(supplement));
        }

        List<Supplement> content = queryFactory
                .select(supplement) // selectFrom(supplement) 대신 select(supplement) 사용
                .from(supplement)
                .leftJoin(supplement.brand, brand).fetchJoin()
                .leftJoin(supplement.supplementIngredients, supplementIngredient).fetchJoin()
                .leftJoin(supplementIngredient.ingredient, ingredient).fetchJoin()
                .where(
                        unifiedSearch(keyword),
                        hasBrandName(brandName),
                        hasIngredientName(ingredientName)
                )
                .distinct()
                // 4. 동적으로 계산된 인기도 점수와 가나다순으로 정렬
                .orderBy(
                        new OrderSpecifier<>(Order.DESC, popularityScore, OrderSpecifier.NullHandling.NullsLast), //1순위: 인기순
                        supplement.name.asc()               // 2순위: 가나다순
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
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