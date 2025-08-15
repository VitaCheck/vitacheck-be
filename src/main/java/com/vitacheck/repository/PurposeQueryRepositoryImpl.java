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

        // 1) (purpose, ingredientId) 쌍을 DISTINCT로 페이징
        //    ※ 보충제 연결이 있는 성분만 포함하도록 supplementIngredients 조인 추가
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

        // 1-1) 총 개수(동일 기준)  ※ concat distinct이 느리면 서브쿼리로 대체 가능
        Long total = queryFactory
                .select(Expressions.numberTemplate(Long.class,
                        "count(distinct concat({0},'-',{1}))",
                        purposeCategory.name.stringValue(), ingredient.id))
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .join(ingredient.supplementIngredients, supplementIngredient)
                .where(purposeFilter)
                .fetchOne();

        // 2) OR 폭탄 제거: ingredientIds만 추려서, 요청 목적과 함께 단순 조건으로 본문 조회
        List<Long> ingredientIds = keys.stream()
                .map(t -> t.get(1, Long.class))
                .distinct()
                .toList();

        List<PurposeIngredientSupplementRow> content = queryFactory
                .select(Projections.constructor(
                        PurposeIngredientSupplementRow.class,
                        ingredient.id,
                        ingredient.name,
                        purposeCategory.name.stringValue(),   // "EYE"/"SKIN" 등
                        supplement.id,
                        supplement.name,
                        supplement.imageUrl
                ))
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .join(ingredient.supplementIngredients, supplementIngredient)
                .join(supplementIngredient.supplement, supplement)
                .where(
                        ingredient.id.in(ingredientIds),
                        purposeFilter
                )
                .orderBy(purposeCategory.name.asc(), ingredient.name.asc(), supplement.name.asc())
                .fetch();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
