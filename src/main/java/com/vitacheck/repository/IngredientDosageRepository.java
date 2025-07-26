package com.vitacheck.repository;

import com.vitacheck.domain.IngredientDosage;
import com.vitacheck.domain.user.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IngredientDosageRepository extends JpaRepository<IngredientDosage, Long> {

    /**
     * 주어진 성분 ID 리스트와 사용자의 연령/성별에 맞는 모든 섭취 기준을 조회합니다.
     */
    @Query("SELECT d FROM IngredientDosage d WHERE d.ingredient.id IN :ingredientIds " +
            "AND d.gender IN (:gender, 'ALL') " +
            "AND :age BETWEEN d.minAge AND d.maxAge")
    List<IngredientDosage> findApplicableDosages(
            @Param("ingredientIds") List<Long> ingredientIds,
            @Param("gender") Gender gender,
            @Param("age") int age);
}