package com.vitacheck.Activity.service;

import com.vitacheck.Activity.repository.IngredientLikeRepository;
import com.vitacheck.Activity.domain.Like.IngredientLike;
import com.vitacheck.Activity.dto.LikedIngredientResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredientLikeQueryService {

    private final IngredientLikeRepository ingredientLikeRepository;

    public List<LikedIngredientResponseDto> getLikedIngredientsByUserId(Long userId) {
        List<IngredientLike> likes = ingredientLikeRepository.findAllByUserId(userId);
        return likes.stream()
                .map(like -> LikedIngredientResponseDto.from(like.getIngredient()))
                .toList();
    }
}
