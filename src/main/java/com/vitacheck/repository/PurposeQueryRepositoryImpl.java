package com.vitacheck.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vitacheck.domain.purposes.AllPurpose;
import com.vitacheck.dto.PurposeIngredientSupplementRow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static com.vitacheck.domain.QSupplement.supplement;
import static com.vitacheck.domain.QIngredient.ingredient;
import static com.vitacheck.domain.mapping.QSupplementIngredient.supplementIngredient;
import static com.vitacheck.domain.purposes.QPurposeCategory.purposeCategory;

@Repository
@RequiredArgsConstructor
public class PurposeQueryRepositoryImpl implements PurposeQueryRepository {

    private final JPAQueryFactory queryFactory;

    // 1. 성분 아이디만 페이지네이션
    @Override
    public Slice<Long> findIngredientIdPageByPurposes(List<AllPurpose> purposes, Pageable pageable) {
        BooleanExpression purposeFilter = (purposes == null || purposes.isEmpty())
                ? null
                : purposeCategory.name.in(purposes);

        List<Long> ingredientIds = queryFactory
                .select(ingredient.id)
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .where(
                        purposeFilter,
                        JPAExpressions.selectOne()
                                .from(supplementIngredient)
                                .where(supplementIngredient.ingredient.eq(ingredient))
                                .exists()
                )
                .distinct()
                .orderBy(ingredient.name.asc(), ingredient.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1) // <--- 핵심 변경점
                .fetch();

        // 얇은 페이지: 성분 ID만
        boolean hasNext = false;
        if (ingredientIds.size() > pageable.getPageSize()) {
            ingredientIds.remove(pageable.getPageSize()); // 마지막 데이터(다음 페이지 확인용)는 제거
            hasNext = true;
        }

        return new SliceImpl<>(ingredientIds, pageable, hasNext);
    }

    // 2. 목적을 기준으로 영양소 필터링
    @Override
    public Map<Long, List<AllPurpose>> findPurposesByIngredientIds(Collection<Long> ingredientIds,
                                                                   @Nullable List<AllPurpose> filterPurposes) {
        if (ingredientIds.isEmpty()) return Map.of();

        BooleanExpression filter = (filterPurposes == null || filterPurposes.isEmpty())
                ? null
                : purposeCategory.name.in(filterPurposes);

        List<Tuple> rows = queryFactory
                .select(ingredient.id, purposeCategory.name)
                .from(ingredient)
                .join(ingredient.purposeCategories, purposeCategory)
                .where(ingredient.id.in(ingredientIds), filter)
                .fetch();

        return rows.stream().collect(Collectors.groupingBy(
                t -> t.get(ingredient.id),
                LinkedHashMap::new,
                Collectors.mapping(t -> t.get(purposeCategory.name), Collectors.toList())
        ));
    }

    // 3. 영양소로 영양제 필터링 
    @Getter
    @AllArgsConstructor
    public static class SupplementBriefRow {
        private Long ingredientId;
        private Long supplementId;
        private String supplementName;
        private String supplementImageUrl;
    }

    public Map<Long, List<SupplementBriefRow>> findSupplementsByIngredientIds(Collection<Long> ingredientIds) {
        if (ingredientIds.isEmpty()) return Map.of();

        // 목적 조인 없이 성분-보충제만
        List<SupplementBriefRow> rows = queryFactory
                .select(Projections.constructor(
                        SupplementBriefRow.class,
                        ingredient.id,
                        supplement.id,
                        supplement.name,
                        supplement.imageUrl
                ))
                .from(supplementIngredient)
                .join(supplementIngredient.ingredient, ingredient)
                .join(supplementIngredient.supplement, supplement)
                .where(ingredient.id.in(ingredientIds))
                .distinct()
                .fetch();

        Map<Long, List<SupplementBriefRow>> map = new LinkedHashMap<>();
        for (SupplementBriefRow r : rows) {
            map.computeIfAbsent(r.getIngredientId(), k -> new ArrayList<>()).add(r);
        }
        return map;
    }

    // 성분명 조회
    public Map<Long, String> findIngredientNames(Collection<Long> ingredientIds) {
        if (ingredientIds.isEmpty()) return Map.of();
        List<Tuple> rows = queryFactory
                .select(ingredient.id, ingredient.name)
                .from(ingredient)
                .where(ingredient.id.in(ingredientIds))
                .fetch();

        Map<Long, String> map = new HashMap<>(rows.size() * 2);
        for (Tuple t : rows) map.put(t.get(ingredient.id), t.get(ingredient.name));
        return map;
    }

}
