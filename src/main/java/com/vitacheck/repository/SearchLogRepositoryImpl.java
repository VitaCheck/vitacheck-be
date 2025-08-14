package com.vitacheck.repository;

import com.querydsl.core.Tuple; // Tuple import
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vitacheck.domain.QSupplement;
import com.vitacheck.domain.searchLog.QSearchLog;
import com.vitacheck.domain.searchLog.SearchCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SearchLogRepositoryImpl implements SearchLogRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Tuple> findPopularIngredients(int limit) { // 반환 타입을 List<Tuple>로 변경
        QSearchLog searchLog = QSearchLog.searchLog;

        return queryFactory
                // 👇 DTO 대신 조회할 컬럼들을 직접 나열합니다.
                .select(
                        searchLog.keyword,
                        searchLog.keyword.count()
                )
                .from(searchLog)
                .where(
                        searchLog.category.eq(SearchCategory.INGREDIENT)
                )
                .groupBy(searchLog.keyword)
                .orderBy(searchLog.keyword.count().desc())
                .limit(limit)
                .fetch();
    }


    @Override
    public Page<Tuple> findPopularSupplements(Integer startAge, Integer endAge, Pageable pageable) {
        QSearchLog searchLog = QSearchLog.searchLog;
        QSupplement supplement = QSupplement.supplement;

        // 데이터 조회 쿼리
        List<Tuple> content = queryFactory
                .select(
                        supplement,
                        searchLog.keyword.count()
                )
                .from(searchLog)
                .join(supplement).on(searchLog.keyword.eq(supplement.name)) // 로그의 키워드와 영양제 이름을 조인
                .where(
                        searchLog.category.eq(SearchCategory.SUPPLEMENT), // 영양제 검색 로그만 필터링
                        ageCondition(startAge, endAge)                   // 동적 연령대 필터링
                )
                .groupBy(supplement) // 영양제 객체로 그룹화
                .orderBy(searchLog.keyword.count().desc()) // 검색 횟수가 많은 순으로 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 조회 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(supplement.countDistinct())
                .from(searchLog)
                .join(supplement).on(searchLog.keyword.eq(supplement.name))
                .where(
                        searchLog.category.eq(SearchCategory.SUPPLEMENT),
                        ageCondition(startAge, endAge)
                );

        return new PageImpl<>(content, pageable, countQuery.fetchOne());
    }

    // 연령대 조건을 동적으로 생성하는 private 메소드
    private BooleanExpression ageCondition(Integer startAge, Integer endAge) {
        if (startAge == null || endAge == null) {
            return null; // "전체" 연령대일 경우 조건을 적용하지 않음
        }
        return QSearchLog.searchLog.age.between(startAge, endAge);
    }
}