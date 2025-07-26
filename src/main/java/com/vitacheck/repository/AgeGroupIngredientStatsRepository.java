package com.vitacheck.repository;

import com.vitacheck.domain.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgeGroupIngredientStatsRepository extends JpaRepository<com.vitacheck.domain.AgeGroupIngredientStats, Long> {
    Optional<com.vitacheck.domain.AgeGroupIngredientStats> findByIngredientAndAge(Ingredient ingredient, String age);

}
