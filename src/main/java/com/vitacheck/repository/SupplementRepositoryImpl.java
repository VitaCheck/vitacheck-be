package com.vitacheck.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.searchLog.SearchCategory;
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
import static com.vitacheck.domain.searchLog.QSearchLog.searchLog; // QSearchLog import

@Repository
@RequiredArgsConstructor
public class SupplementRepositoryImpl implements SupplementRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Supplement> search(User user, String keyword, String brandName, String ingredientName, Pageable pageable) {

        Expression<Long> popularityScore;

        if (user != null) {
            // --- 1. 연령대 문자열을 숫자 범위로 변환하는 로직 추가 ---
            String ageGroupString = DateUtils.calculateAgeGroup(user.getBirthDate()); // "20대", "30대" 등
            int startAge = 0;
            int endAge = 150; // 기본값은 매우 넓은 범위로 설정

            if (ageGroupString != null && ageGroupString.contains("대")) {
                try {
                    int decade = Integer.parseInt(ageGroupString.replace("대", "")); // "20대" -> 20
                    startAge = decade;
                    endAge = decade + 9; // 20대 -> 20 ~ 29
                } catch (NumberFormatException e) {
                    // "알 수 없음" 등의 예외 상황 처리
                }
            }
            // --------------------------------------------------------

            popularityScore = JPAExpressions.select(searchLog.count())
                    .from(searchLog)
                    .where(
                            searchLog.keyword.eq(supplement.name)
                                    .and(searchLog.category.eq(SearchCategory.SUPPLEMENT))
                                    // --- 2. eq를 between으로 수정 ---
                                    .and(searchLog.age.between(startAge, endAge)) // age가 startAge와 endAge 사이에 있는지 확인
                                    .and(searchLog.gender.eq(user.getGender()))
                    );
        } else {
            // [비로그인 사용자] 모든 사용자의 로그 카운트 합계를 점수로 사용
            popularityScore = JPAExpressions.select(searchLog.count())
                    .from(searchLog)
                    .where(
                            searchLog.keyword.eq(supplement.name)
                                    .and(searchLog.category.eq(SearchCategory.SUPPLEMENT))
                    );
        }
        // -------------------------------------------


        List<Supplement> content = queryFactory
                .select(supplement)
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
                // --- 2. 동적으로 계산된 인기도 점수와 가나다순으로 정렬 ---
                .orderBy(
                        new OrderSpecifier<>(Order.DESC, popularityScore, OrderSpecifier.NullHandling.NullsLast), // 1순위: 인기도
                        supplement.name.asc()               // 2순위: 가나다순
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        // 전체 카운트 조회를 위한 쿼리 (정렬은 필요 없으므로 기존 코드 유지)
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

    // ... 기존 private 메소드들은 그대로 유지 ...
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