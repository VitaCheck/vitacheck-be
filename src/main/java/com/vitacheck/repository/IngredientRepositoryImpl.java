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
        // 데이터 조회 쿼리: 특정 목적을 가진 성분 목록을 페이징하여 가져옵니다.
        List<Ingredient> content = queryFactory
                .select(ingredient)
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .where(purposeCategory.name.in(purposeNames))
                .distinct() // 중복 제거
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 쿼리
        long total = queryFactory
                .select(ingredient.countDistinct())
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .where(purposeCategory.name.in(purposeNames))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}