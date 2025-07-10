package com.vitacheck.service;

import com.vitacheck.domain.Like;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.LikeToggleResponseDto;
import com.vitacheck.repository.LikeRepository;
import com.vitacheck.repository.SupplementRepository;
import com.vitacheck.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeCommandService {

    private final LikeRepository likeRepository;
    private final SupplementRepository supplementRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeToggleResponseDto toggleLike(Long supplementId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다.")); // TODO: 나중에 에러응답 통일되면 교체할 것


        Supplement supplement = supplementRepository.findById(supplementId)
                .orElseThrow(() -> new EntityNotFoundException("영양제를 찾을 수 없습니다.")); // TODO: 나중에 에러응답 통일되면 교체할 것


        return likeRepository.findByUserAndSupplement(user, supplement)
                .map(existingLike -> {
                    likeRepository.delete(existingLike);
                    return LikeToggleResponseDto.builder()
                            .supplementId(supplementId)
                            .liked(false)
                            .build();
                })
                .orElseGet(() -> {
                    likeRepository.save(Like.builder()
                            .user(user)
                            .supplement(supplement)
                            .build());
                    return LikeToggleResponseDto.builder()
                            .supplementId(supplementId)
                            .liked(true)
                            .build();
                });
    }
}