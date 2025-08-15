package com.vitacheck.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
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

        var purposeFilter = (purposes == null || purposes.isEmpty())
                ? null
                : purposeCategory.name.in(purposes);

        // 1) (purpose, ingredient) 키를 DISTINCT로 페이징
        //    ✅ 보충제 연결 존재 조건을 포함시켜야 함
        var keys = queryFactory
                .select(purposeCategory.name.stringValue(), ingredient.id)
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .join(ingredient.supplementIngredients, supplementIngredient)
                .where(purposeFilter)
                .distinct()
                .orderBy(purposeCategory.name.asc(), ingredient.name.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (keys.isEmpty()) {
            Long total0 = queryFactory
                    .select(Expressions.numberTemplate(Long.class,
                            "count(distinct concat({0},'-',{1}))",
                            purposeCategory.name.stringValue(), ingredient.id))
                    .from(ingredient)
                    .join(ingredient.purposeCategories, purposeCategory)
                    .join(ingredient.supplementIngredients, supplementIngredient)
                    .where(purposeFilter)
                    .fetchOne();
            return new PageImpl<>(List.of(), pageable, total0 == null ? 0 : total0);
        }

        // total도 동일 기준으로 계산
        Long total = queryFactory
                .select(Expressions.numberTemplate(Long.class,
                        "count(distinct concat({0},'-',{1}))",
                        purposeCategory.name.stringValue(), ingredient.id))
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .join(ingredient.supplementIngredients, supplementIngredient)
                .where(purposeFilter)
                .fetchOne();

        // 2) 현재 페이지의 (purpose, ingredient) "쌍"만 본문에서 가져오도록 정확히 제한
        List<String> pairKeys = keys.stream()
                .map(t -> t.get(0, String.class) + "-" + t.get(1, Long.class))     // "EYE-123"
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
                .orderBy(purposeCategory.name.asc(), ingredient.name.asc(), supplement.name.asc())
                .fetch();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
