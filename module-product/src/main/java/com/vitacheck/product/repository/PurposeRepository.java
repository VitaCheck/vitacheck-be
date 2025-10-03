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

    //목적에 맞는 성분을 최대 3개 조회하고, 성분당 영양제 최대 10개까지 조회

    @Query(value = """
    SELECT *
    FROM (
        SELECT i.id   as ingredientId,
               i.name as ingredientName,
               p.id   as purposeId,
               p.name as purposeName,
               ROW_NUMBER() OVER (PARTITION BY p.id ORDER BY i.id) as rn_ingredient
        FROM purposes p
        JOIN purpose_ingredients pi ON p.id = pi.purpose_id
        JOIN ingredients i ON pi.ingredient_id = i.id
        WHERE p.enum_code IN (:goals)
    ) sub
    WHERE rn_ingredient <= 3   -- 목적당 성분 3개 제한
    """, nativeQuery = true)
    List<Object[]> findTop3IngredientsByPurpose(@Param("goals") List<String> goalsEnum);

    @Query(value = """
    SELECT *
    FROM (
        SELECT s.id   as supplementId,
               s.name as supplementName,
               s.coupang_url,
               s.image_url,
               si.ingredient_id as ingredientId,
               ROW_NUMBER() OVER (PARTITION BY si.ingredient_id ORDER BY s.id DESC) as rn_supplement
        FROM supplements s
        JOIN supplement_ingredients si ON s.id = si.supplement_id
        WHERE si.ingredient_id IN (:ingredientIds)
    ) sub
    WHERE rn_supplement <= 10   -- 성분당 영양제 10개 제한
    """, nativeQuery = true)
    List<Object[]> findTop10SupplementsByIngredients(@Param("ingredientIds") List<Long> ingredientIds);




}
