package com.vitacheck.product.repository;

import com.vitacheck.common.enums.Gender;
import com.vitacheck.product.domain.Ingredient.Ingredient;
import com.vitacheck.product.domain.Ingredient.IngredientDosage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByName(String name);
    List<Ingredient> findByNameContainingIgnoreCase(String keyword);
    String existsByName(String name);

    // 성분 id로 ingredientDosage 테이블에서 값 전부 가져오는 기능
    @Query("SELECT d FROM IngredientDosage d WHERE d.ingredient.id = :ingredientId")
    List<IngredientDosage> findDosagesByIngredientId(@Param("ingredientId") Long ingredientId);

    // 단일 최적값 조회
    @Query("SELECT d FROM IngredientDosage d " +
            "WHERE d.ingredient.id = :ingredientId " +
            "AND (:age BETWEEN d.minAge AND d.maxAge) " +
            "AND (d.gender = :gender OR d.gender = com.vitacheck.common.enums.Gender.ALL) " +
            "ORDER BY d.gender DESC")
    Optional<IngredientDosage> findBestDosage(
            @Param("ingredientId") Long ingredientId,
            @Param("gender") Gender gender,
            @Param("age") int age
    );

}
