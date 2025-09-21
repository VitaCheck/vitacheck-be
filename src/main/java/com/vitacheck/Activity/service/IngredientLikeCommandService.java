package com.vitacheck.Activity.service;

import com.vitacheck.Activity.repository.IngredientLikeRepository;
import com.vitacheck.Activity.domain.Like.IngredientLike;
import com.vitacheck.Activity.dto.IngredientLikeToggleResponseDto;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.product.domain.Ingredient.Ingredient;
import com.vitacheck.product.repository.IngredientRepository;
import com.vitacheck.user.domain.User;
import com.vitacheck.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IngredientLikeCommandService {

    private final IngredientRepository ingredientRepository;
    private final IngredientLikeRepository ingredientLikeRepository;
    private final UserRepository userRepository;

    @Transactional
    public IngredientLikeToggleResponseDto toggleIngredientLike(Long ingredientId, Long userId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new CustomException(ErrorCode.INGREDIENT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return ingredientLikeRepository.findByUserAndIngredient(user, ingredient)
                .map(existing -> {
                    ingredientLikeRepository.delete(existing);
                    return IngredientLikeToggleResponseDto.builder()
                            .ingredientId(ingredientId)
                            .liked(false)
                            .build();
                })
                .orElseGet(() -> {
                    IngredientLike newLike = IngredientLike.builder()
                            .user(user)
                            .ingredient(ingredient)
                            .build();
                    ingredientLikeRepository.save(newLike);
                    return IngredientLikeToggleResponseDto.builder()
                            .ingredientId(ingredientId)
                            .liked(true)
                            .build();
                });
    }
}
