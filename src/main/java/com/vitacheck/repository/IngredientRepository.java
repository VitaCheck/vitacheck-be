package com.vitacheck.repository;

import com.vitacheck.domain.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByName(String name);
    List<Ingredient> findByNameContainingIgnoreCase(String keyword);

}
