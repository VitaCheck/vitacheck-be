package com.vitacheck.repository;

import com.vitacheck.domain.mapping.IngredientAlternativeFood;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientAlternativeFoodRepository extends JpaRepository<IngredientAlternativeFood, Long> {
}
