package com.vitacheck.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vitacheck.domain.QIngredient;
import com.vitacheck.domain.QSupplement;
import com.vitacheck.domain.mapping.QIngredientCategory;
import com.vitacheck.domain.mapping.QSupplementIngredient;
import com.vitacheck.domain.purposes.AllPurpose;
import com.vitacheck.domain.purposes.PurposeCategory;
import com.vitacheck.domain.purposes.QPurposeCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PurposeCategoryQueryRepositoryImpl implements PurposeCategoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PurposeCategory> findAllWithIngredientAndSupplementByNameIn(List<AllPurpose> names) {
        QPurposeCategory pc = QPurposeCategory.purposeCategory;
        QIngredientCategory ic = QIngredientCategory.ingredientCategory;
        QIngredient i = QIngredient.ingredient;
        QSupplementIngredient si = QSupplementIngredient.supplementIngredient;
        QSupplement s = QSupplement.supplement;

        return queryFactory
                .selectFrom(pc)
                .distinct()
                .join(pc.ingredientCategories, ic).fetchJoin()
                .join(ic.ingredient, i).fetchJoin()
                .join(i.supplementIngredients, si).fetchJoin()
                .join(si.supplement, s).fetchJoin()
                .where(pc.name.in(names))
                .fetch();
    }
}

