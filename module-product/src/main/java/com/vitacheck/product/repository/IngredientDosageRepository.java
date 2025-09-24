package com.vitacheck.product.repository;

import com.vitacheck.common.enums.Gender;
import com.vitacheck.product.domain.Ingredient.IngredientDosage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IngredientDosageRepository extends JpaRepository<IngredientDosage, Long> {

    /**
     * 주어진 성분 ID 리스트와 사용자의 연령/성별에 맞는 모든 섭취 기준을 조회합니다.
     */
    @Query("SELECT d FROM IngredientDosage d " +
            "WHERE d.ingredient.id IN :ingredientIds " +
            "AND d.gender IN (:gender, 'ALL') " +
            "AND :age BETWEEN d.minAge AND d.maxAge")
    List<IngredientDosage> findApplicableDosages(
            @Param("ingredientIds") List<Long> ingredientIds,
            @Param("gender") Gender gender,
            @Param("age") int age);

    @Query("SELECT d FROM IngredientDosage d " +
            "WHERE d.ingredient.id = :ingredientId " +
            "AND (:age BETWEEN d.minAge AND d.maxAge) " +
            "AND d.gender IN (:gender, com.vitacheck.common.enums.Gender.ALL) " +
            "ORDER BY d.gender DESC") // MALE/FEMALE이 ALL보다 우선순위가 높도록 정렬
    List<IngredientDosage> findApplicableDosages(
            @Param("ingredientId") Long ingredientId,
            @Param("gender") Gender gender,
            @Param("age") int age);



    default Optional<IngredientDosage> findBestDosage(Long ingredientId, Gender gender, int age) {
        // 우선순위대로 정렬된 목록에서 가장 첫 번째 결과를 반환합니다.
        return findApplicableDosages(ingredientId, gender, age)
                .stream()
                .findFirst();
    }

    // 1) 유저 조건 기반 조회 (성별/나이)
    @Query("""
      select d
      from IngredientDosage d
      where d.ingredient.id in :ingredientIds
        and (d.gender = :gender or d.gender = com.vitacheck.common.enums.Gender.ALL)
        and d.minAge <= :age and d.maxAge >= :age
    """)
    List<IngredientDosage> findDosagesByUserCondition(
            @Param("ingredientIds") Collection<Long> ingredientIds,
            @Param("gender") Gender gender,
            @Param("age") int age
    );

    // 2) 일반값 (fallback) 조회 (ALL & 무연령)
    @Query("""
      select d
      from IngredientDosage d
      where d.ingredient.id in :ingredientIds
        and d.gender = Gender.ALL
        and d.minAge is null and d.maxAge is null
    """)
    List<IngredientDosage> findGeneralDosagesByIngredientIds(
            @Param("ingredientIds") Collection<Long> ingredientIds
    );
}
