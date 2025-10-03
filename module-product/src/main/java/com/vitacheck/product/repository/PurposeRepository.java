package com.vitacheck.product.repository;

import com.vitacheck.product.domain.Purpose.AllPurpose;
import com.vitacheck.product.domain.Purpose.Purpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurposeRepository extends JpaRepository<Purpose, Long> {
    List<Purpose> findAll();
    Purpose save(Purpose purpose);
    // 목적에 맞는 성분을 조회하고, 성분으로 영양제 10개까지 조회
    @Query(value = """
    SELECT *
    FROM (
        SELECT p.id   as purposeId,
               p.name as purposeName,
               i.id   as ingredientId,
               i.name as ingredientName,
               s.id   as supplementId,
               s.name as supplementName,
               s.coupang_url,
               s.image_url,
               ROW_NUMBER() OVER (PARTITION BY i.id ORDER BY s.id DESC) as rn_supplement
        FROM purposes p
        JOIN purpose_ingredients pi ON p.id = pi.purpose_id
        JOIN ingredients i ON pi.ingredient_id = i.id
        JOIN supplement_ingredients si ON i.id = si.ingredient_id
        JOIN supplements s ON si.supplement_id = s.id
        WHERE p.enum_code IN (:goals)
    ) sub
    WHERE rn_supplement <= 10   -- 성분당 영양제 10개 제한
    """, nativeQuery = true)
    List<Object[]> findPurposeWithLimitedSupplements(@Param("goals") List<String> goalsEnum);


}
