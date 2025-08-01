package com.vitacheck.service;

import com.vitacheck.domain.SupplementLike;
import com.vitacheck.dto.LikedSupplementResponseDto;
import com.vitacheck.repository.SupplementLikeRepository;
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
