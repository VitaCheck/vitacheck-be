package com.vitacheck.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vitacheck.domain.Ingredient;
import com.vitacheck.domain.purposes.AllPurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.vitacheck.domain.QIngredient.ingredient;
import static com.vitacheck.domain.purposes.QPurposeCategory.purposeCategory;

@Repository
@RequiredArgsConstructor
public class IngredientRepositoryImpl implements IngredientRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Ingredient> findByPurposeNames(List<AllPurpose> purposeNames, Pageable pageable) {

        // 데이터 조회 쿼리
        List<Ingredient> content = queryFactory
                .select(ingredient)
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .where(purposeCategory.name.in(purposeNames))
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(ingredient.countDistinct())
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .where(purposeCategory.name.in(purposeNames));

        long total = countQuery.fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}