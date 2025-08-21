package com.vitacheck.repository;

import com.vitacheck.domain.Ingredient;
import com.vitacheck.domain.IngredientLike;
import com.vitacheck.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface
IngredientLikeRepository extends JpaRepository<IngredientLike, Long> {

    Optional<IngredientLike> findByUserAndIngredient(User user, Ingredient ingredient);

    boolean existsByUserAndIngredient(User user, Ingredient ingredient);

    void deleteByUserAndIngredient(User user, Ingredient ingredient);

    List<IngredientLike> findAllByUserId(Long userId);

    void deleteAllByUser(User user);
}