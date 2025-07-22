package com.vitacheck.service;

import com.vitacheck.domain.Ingredient;
import com.vitacheck.dto.IngredientResponseDTO;
import com.vitacheck.global.apiPayload.CustomException;
import com.vitacheck.global.apiPayload.code.ErrorCode;
import com.vitacheck.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IngredientService {
    private final IngredientRepository ingredientRepository;

    public IngredientResponseDTO.IngredientDetails getIngredientDetails(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.INGREDIENT_NOT_FOUND));

        return IngredientResponseDTO.IngredientDetails.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .description(ingredient.getDescription())
                .effect(ingredient.getEffect())
                .caution(ingredient.getCaution())
                .upper_limit(ingredient.getUpperLimit())
                .lower_limit(ingredient.getLowerLimit())
                .unit(ingredient.getUnit())
                .build();

    }

//    public IngredientFood getIngredientFoods(Long id) {
//        if (!ingredientRepository.existsById(id)) {
//            throw new CustomException(ErrorCode.INGREDIENT_NOT_FOUND);
//        }
//        List<IngredientAlternativeFood> foods = foodRepository.findByIngredientId(id);
//        return IngredientConverter.toFoodDto(id, foods);
//    }

}
