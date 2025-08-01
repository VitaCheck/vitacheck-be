package com.vitacheck.service;

import com.vitacheck.domain.Like;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.LikeToggleResponseDto;
import com.vitacheck.global.apiPayload.CustomException;
import com.vitacheck.global.apiPayload.code.ErrorCode;
import com.vitacheck.repository.LikeRepository;
import com.vitacheck.repository.SupplementRepository;
import com.vitacheck.repository.UserRepository;
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
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


        Supplement supplement = supplementRepository.findById(supplementId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUPPLEMENT_NOT_FOUND));


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