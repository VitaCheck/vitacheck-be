package com.vitacheck.Activity.service;

import com.vitacheck.Activity.repository.SupplementLikeRepository;
import com.vitacheck.Activity.domain.Like.SupplementLike;
import com.vitacheck.Activity.dto.LikedSupplementResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplementLikeQueryService {

    private final SupplementLikeRepository supplementLikeRepository;

    public List<LikedSupplementResponseDto> getLikedSupplementsByUserId(Long userId) {
        List<SupplementLike> supplementLikes = supplementLikeRepository.findAllByUserIdWithSupplement(userId);

        return supplementLikes.stream()
                .map(supplementLike -> LikedSupplementResponseDto.builder()
                        .supplementId(supplementLike.getSupplement().getId())
                        .name(supplementLike.getSupplement().getName())
                        .brandName(supplementLike.getSupplement().getBrand().getName())
                        .imageUrl(supplementLike.getSupplement().getImageUrl())
                        .price(supplementLike.getSupplement().getPrice())
                        .build())
                .toList();
    }
}
