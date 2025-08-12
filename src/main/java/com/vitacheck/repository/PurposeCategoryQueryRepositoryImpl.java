// src/main/java/com/vitacheck/repository/PurposeCategoryQueryRepositoryImpl.java
package com.vitacheck.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
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

        // 👇👇👇 기존의 복잡한 쿼리를 아래 코드로 교체합니다. 👇👇👇
        return queryFactory
                .selectFrom(pc)
                .distinct()
                .leftJoin(pc.ingredients).fetchJoin() // PurposeCategory에서 ingredients로 바로 조인
                .where(pc.name.in(names))
                .fetch();
    }
}