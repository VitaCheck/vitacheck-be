package com.vitacheck.repository;

import com.vitacheck.common.enums.Gender; // ✅ 통일된 Enum으로 import
import com.vitacheck.domain.IngredientDosage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IngredientDosageRepository extends JpaRepository<IngredientDosage, Long> {

    // ✅✅✅ 쿼리를 단순하고 명확하게 수정합니다 ✅✅✅
    @Query("SELECT d FROM IngredientDosage d WHERE d.ingredient.id IN :ingredientIds " +
            "AND d.gender IN :genders " + // gender가 아닌 genders 리스트를 받도록 변경
            "AND :age BETWEEN d.minAge AND d.maxAge")
    List<IngredientDosage> findApplicableDosages(
            @Param("ingredientIds") List<Long> ingredientIds,
            @Param("genders") List<Gender> genders, // 파라미터 타입을 List로 변경
            @Param("age") int age);

    @Query("SELECT d FROM IngredientDosage d " +
            "WHERE d.ingredient.id = :ingredientId " +
            "AND (:age BETWEEN d.minAge AND d.maxAge) " +
            "AND d.gender IN :genders " + // genders 리스트를 받도록 변경
            "ORDER BY d.gender DESC")
    List<IngredientDosage> findApplicableDosages(
            @Param("ingredientId") Long ingredientId,
            @Param("genders") List<Gender> genders, // 파라미터 타입을 List로 변경
            @Param("age") int age);

    default Optional<IngredientDosage> findBestDosage(Long ingredientId, Gender gender, int age) {
        // ✅ 서비스 로직에 있던 것을 이쪽으로 가져와도 괜찮습니다.
        return findApplicableDosages(ingredientId, List.of(gender, Gender.ALL), age)
                .stream()
                .findFirst();
    }
}