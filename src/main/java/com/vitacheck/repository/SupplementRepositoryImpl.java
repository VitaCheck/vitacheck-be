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
import com.vitacheck.domain.purposes.AllPurpose;

import java.util.Collections;
import java.util.List;

import static com.vitacheck.domain.QBrand.brand;
import static com.vitacheck.domain.QIngredient.ingredient;
import static com.vitacheck.domain.QSupplement.supplement;
import static com.vitacheck.domain.mapping.QSupplementIngredient.supplementIngredient;
import static com.vitacheck.domain.searchLog.QSearchLog.searchLog;
import static com.vitacheck.domain.purposes.QPurposeCategory.purposeCategory;


@Repository
@RequiredArgsConstructor
public class SupplementRepositoryImpl implements SupplementRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Supplement> search(User user, String keyword, String brandName, String ingredientName, Pageable pageable) {

        // 1단계: 현재 페이지에 해당하는 영양제의 ID 목록만 조회 (가벼운 쿼리)
        List<Long> ids = queryFactory
                .select(supplement.id)
                .from(supplement)
                .leftJoin(supplement.brand, brand)
                .leftJoin(supplement.supplementIngredients, supplementIngredient)
                .leftJoin(supplementIngredient.ingredient, ingredient)
                .where(
                        unifiedSearch(keyword),
                        hasBrandName(brandName),
                        hasIngredientName(ingredientName)
                )
                .distinct()
                .orderBy(getOrderBySpecifiers(user)) // 정렬 조건을 위한 헬퍼 메서드 사용
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (ids.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 2단계: 위에서 얻은 ID 목록을 사용해 필요한 전체 데이터를 fetch join으로 조회
        List<Supplement> content = queryFactory
                .selectFrom(supplement)
                .leftJoin(supplement.brand, brand).fetchJoin()
                .leftJoin(supplement.supplementIngredients, supplementIngredient).fetchJoin()
                .leftJoin(supplementIngredient.ingredient, ingredient).fetchJoin()
                .where(supplement.id.in(ids))
                .distinct()
                .orderBy(getOrderBySpecifiers(user)) // 동일한 정렬 조건 적용
                .fetch();

        // 전체 카운트 쿼리는 기존 로직 유지
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

    // 정렬 조건 생성 로직을 별도 메서드로 분리하여 재사용성 증대
    private OrderSpecifier<?>[] getOrderBySpecifiers(User user) {
        Expression<Long> popularityScore;

        if (user != null && user.getBirthDate() != null) {
            String ageGroupString = DateUtils.calculateAgeGroup(user.getBirthDate());
            int startAge = 0;
            int endAge = 150;

            if (ageGroupString.contains("대")) {
                try {
                    int decade = Integer.parseInt(ageGroupString.replace("대", ""));
                    startAge = decade;
                    endAge = decade + 9;
                } catch (NumberFormatException e) {
                    // "알 수 없음" 등의 예외 상황 처리
                }
            }

            popularityScore = JPAExpressions.select(searchLog.count())
                    .from(searchLog)
                    .where(
                            searchLog.keyword.eq(supplement.name),
                            searchLog.category.eq(SearchCategory.SUPPLEMENT),
                            searchLog.age.between(startAge, endAge),
                            searchLog.gender.eq(user.getGender())
                    );
        } else {
            popularityScore = JPAExpressions.select(searchLog.count())
                    .from(searchLog)
                    .where(
                            searchLog.keyword.eq(supplement.name),
                            searchLog.category.eq(SearchCategory.SUPPLEMENT)
                    );
        }

        return new OrderSpecifier[]{
                new OrderSpecifier<>(Order.DESC, popularityScore, OrderSpecifier.NullHandling.NullsLast),
                supplement.name.asc()
        };
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

    // [신규 구현] 목적별 영양제를 페이징하여 조회하는 Querydsl 메서드
    @Override
    public Page<Supplement> findByPurposeNames(List<AllPurpose> purposeNames, Pageable pageable) {

        // 1단계: 현재 페이지에 해당하는 영양제의 ID 목록만 조회
        List<Long> ids = queryFactory
                .select(supplement.id)
                .from(supplement)
                .leftJoin(supplement.supplementIngredients, supplementIngredient)
                .leftJoin(supplementIngredient.ingredient, ingredient)
                .leftJoin(ingredient.purposeCategories, purposeCategory)
                .where(purposeCategory.name.in(purposeNames))
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (ids.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 2단계: 조회된 ID 목록을 기반으로 전체 영양제 데이터와 연관 엔티티를 fetch join으로 조회
        List<Supplement> content = queryFactory
                .selectFrom(supplement)
                .join(supplement.brand, brand).fetchJoin()
                .join(supplement.supplementIngredients, supplementIngredient).fetchJoin()
                .join(supplementIngredient.ingredient, ingredient).fetchJoin()
                .where(supplement.id.in(ids))
                .distinct()
                .fetch();

        // 총 카운트 쿼리
        long total = queryFactory
                .select(supplement.countDistinct())
                .from(supplement)
                .leftJoin(supplement.supplementIngredients, supplementIngredient)
                .leftJoin(supplementIngredient.ingredient, ingredient)
                .leftJoin(ingredient.purposeCategories, purposeCategory)
                .where(purposeCategory.name.in(purposeNames))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}