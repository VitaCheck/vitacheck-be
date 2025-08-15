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

        // 1) (purpose, ingredientId) 쌍을 distinct로 페이지 자르기
        //    결과: keys = [[purposeName(String), ingredientId(Long)], ...]
        var keys = queryFactory
                .select(purposeCategory.name.stringValue(), ingredient.id)
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
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
                    .where(purposeFilter)
                    .fetchOne();
            return new PageImpl<>(List.of(), pageable, total0 == null ? 0 : total0);
        }

        // total: (purpose, ingredientId) 쌍 개수
        Long total = queryFactory
                .select(Expressions.numberTemplate(Long.class,
                        "count(distinct concat({0},'-',{1}))",
                        purposeCategory.name.stringValue(), ingredient.id))
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .where(purposeFilter)
                .fetchOne();

        // keys → (purpose=..., ingredientId=...) 조건의 OR 묶음
        List<BooleanExpression> ors = new ArrayList<>(keys.size());
        for (var t : keys) {
            String p = t.get(0, String.class);
            Long ingId = t.get(1, Long.class);
            ors.add(purposeCategory.name.stringValue().eq(p).and(ingredient.id.eq(ingId)));
        }
        BooleanExpression pairPredicate = ors.stream().reduce(BooleanExpression::or).orElse(null);

        // 2) 방금 자른 (purpose, ingredient) 쌍들에 한해 평평한 행 전량 조회 (여기선 limit 없음)
        List<PurposeIngredientSupplementRow> content = queryFactory
                .select(Projections.constructor(
                        PurposeIngredientSupplementRow.class,
                        ingredient.id,
                        ingredient.name,
                        purposeCategory.name.stringValue(),   // "EYE"/"SKIN"
                        supplement.id,
                        supplement.name,
                        supplement.imageUrl
                ))
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .join(ingredient.supplementIngredients, supplementIngredient)
                .join(supplementIngredient.supplement, supplement)
                .where(pairPredicate)
                .orderBy(purposeCategory.name.asc(), ingredient.name.asc(), supplement.name.asc())
                .fetch();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
