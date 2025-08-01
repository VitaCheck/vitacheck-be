package com.vitacheck.service;

import com.vitacheck.domain.IngredientLike;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.LikedIngredientResponseDto;
import com.vitacheck.repository.IngredientLikeRepository;
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