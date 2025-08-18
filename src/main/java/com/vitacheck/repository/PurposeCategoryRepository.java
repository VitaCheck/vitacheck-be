package com.vitacheck.repository;

import com.vitacheck.domain.purposes.PurposeCategory;
import com.vitacheck.domain.purposes.AllPurpose;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PurposeCategoryRepository extends JpaRepository<PurposeCategory, Long>, PurposeCategoryQueryRepository {
    Optional<PurposeCategory> findByName(AllPurpose name);
    List<PurposeCategory> findAllByNameIn(List<AllPurpose> names);


    @Query(value = """
        SELECT *
        FROM (
            SELECT pc.name as purposeName,
                   i.id   as ingredientId,
                   i.name as ingredientName,
                   s.id   as supplementId,
                   s.name as supplementName,
                   s.coupang_url,
                   s.image_url,
                   ROW_NUMBER() OVER (PARTITION BY i.id ORDER BY s.id DESC) as rn
            FROM purpose_categories pc
            JOIN purpose_ingredient pi ON pc.id = pi.purpose_id
            JOIN ingredients i ON pi.ingredient_id = i.id
            JOIN supplement_ingredients si ON i.id = si.ingredient_id
            JOIN supplements s ON si.supplement_id = s.id
            WHERE pc.name IN (:goals)
        ) sub
        WHERE rn <= 10
        """, nativeQuery = true)
    List<Object[]> findPurposeWithLimitedSupplements(@Param("goals") List<String> goals);
}
