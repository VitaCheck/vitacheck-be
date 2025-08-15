package com.vitacheck.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vitacheck.domain.purposes.AllPurpose;
import com.vitacheck.dto.PurposeIngredientSupplementRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.vitacheck.domain.QSupplement.supplement;
import static com.vitacheck.domain.QIngredient.ingredient;
import static com.vitacheck.domain.mapping.QSupplementIngredient.supplementIngredient;
import static com.vitacheck.domain.purposes.QPurposeCategory.purposeCategory;
// import static com.vitacheck.domain.mapping.QIngredientCategory.ingredientCategory;

@Repository
@RequiredArgsConstructor
public class PurposeQueryRepositoryImpl implements PurposeQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PurposeIngredientSupplementRow> findByPurposes(List<AllPurpose> purposes, Pageable pageable) {

        BooleanExpression purposeFilter = (purposes == null || purposes.isEmpty())
                ? null
                : purposeCategory.name.in(purposes);

        // ✦ 1) supplement 연결 '존재'만 보장 (행 폭증 방지)
        var hasAnySupplement = JPAExpressions.selectOne()
                .from(supplementIngredient)
                .where(supplementIngredient.ingredient.eq(ingredient))
                .exists();

        // ✦ 2) (purpose, ingredient) 키를 결정적으로 페이징
        var keys = queryFactory
                .select(purposeCategory.name.stringValue(), ingredient.id, ingredient.name)
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .where(purposeFilter, hasAnySupplement)
                .distinct()
                // ▼ 결정성 있는 정렬: name → ingredient.name → ingredient.id(타이브레이커)
                .orderBy(
                        purposeCategory.name.asc(),
                        ingredient.name.asc(),
                        ingredient.id.asc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 페이지 범위를 벗어난 경우: 총합만 계산하고 빈 페이지 반환
        if (keys.isEmpty()) {
            Long total0 = queryFactory
                    .select(Expressions.numberTemplate(Long.class,
                            "count(distinct concat({0},'-',{1}))",
                            purposeCategory.name.stringValue(), ingredient.id))
                    .from(ingredient)
                    .join(ingredient.purposeCategories, purposeCategory)
                    .where(purposeFilter, hasAnySupplement)
                    .fetchOne();
            return new PageImpl<>(List.of(), pageable, total0 == null ? 0 : total0);
        }

        // ✦ total(전체 (purpose, ingredient) 쌍 개수)
        Long total = queryFactory
                .select(Expressions.numberTemplate(Long.class,
                        "count(distinct concat({0},'-',{1}))",
                        purposeCategory.name.stringValue(), ingredient.id))
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .where(purposeFilter, hasAnySupplement)
                .fetchOne();

        // ✦ 3) 현재 페이지 쌍만 본문에서 가져오기
        List<String> pairKeys = keys.stream()
                .map(t -> t.get(0, String.class) + "-" + t.get(1, Long.class)) // "EYE-123"
                .toList();

        var pairExpr = Expressions.stringTemplate(
                "concat({0},'-',{1})",
                purposeCategory.name.stringValue(), ingredient.id);

        List<PurposeIngredientSupplementRow> content = queryFactory
                .select(Projections.constructor(
                        PurposeIngredientSupplementRow.class,
                        ingredient.id,
                        ingredient.name,
                        purposeCategory.name.stringValue(),
                        supplement.id,
                        supplement.name,
                        supplement.imageUrl
                ))
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .join(ingredient.supplementIngredients, supplementIngredient)
                .join(supplementIngredient.supplement, supplement)
                .where(
                        purposeFilter,
                        pairExpr.in(pairKeys)
                )
                // 본문 정렬도 키 정렬 + supplement.name으로 고정
                .orderBy(
                        purposeCategory.name.asc(),
                        ingredient.name.asc(),
                        ingredient.id.asc(),
                        supplement.name.asc()
                )
                .fetch();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
