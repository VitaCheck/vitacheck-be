package com.vitacheck.service;

import com.vitacheck.domain.Like;
import com.vitacheck.dto.LikedSupplementResponseDto;
import com.vitacheck.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeQueryService {

    private final LikeRepository likeRepository;

    public List<LikedSupplementResponseDto> getLikedSupplementsByUserId(Long userId) {
        List<Like> likes = likeRepository.findAllByUserIdWithSupplement(userId);

        return likes.stream()
                .map(like -> LikedSupplementResponseDto.builder()
                        .supplementId(like.getSupplement().getId())
                        .name(like.getSupplement().getName())
                        .brandName(like.getSupplement().getBrand().getName())
                        .imageUrl(like.getSupplement().getImageUrl())
                        .price(like.getSupplement().getPrice())
                        .build())
                .toList();
    }
}
