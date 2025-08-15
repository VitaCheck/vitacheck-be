package com.vitacheck.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vitacheck.domain.purposes.AllPurpose;
import com.vitacheck.dto.PurposeIngredientSupplementRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

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

        // content: 평평한 행으로 한 번에 조회
        List<PurposeIngredientSupplementRow> content = queryFactory
                .select(Projections.constructor(
                        PurposeIngredientSupplementRow.class,
                        ingredient.id,
                        ingredient.name,
                        purposeCategory.name.stringValue(),     // Enum 이름(String)으로 가져옴
                        supplement.id,
                        supplement.name,
                        supplement.imageUrl
                ))
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .join(ingredient.supplementIngredients, supplementIngredient)
                .join(supplementIngredient.supplement, supplement)
                .where(purposeCategory.name.in(purposes))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // total: 성분 기준으로 distinct 카운트(가벼운 편)
        Long total = queryFactory
                .select(ingredient.countDistinct())
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .where(purposeCategory.name.in(purposes))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
