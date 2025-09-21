package com.vitacheck.Activity.service;

import com.vitacheck.Activity.repository.SupplementLikeRepository;
import com.vitacheck.Activity.domain.Like.SupplementLike;
import com.vitacheck.Activity.dto.LikeToggleResponseDto;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.product.domain.Supplement.Supplement;
import com.vitacheck.product.repository.SupplementRepository;
import com.vitacheck.user.domain.User;
import com.vitacheck.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplementLikeCommandService {

    private final SupplementLikeRepository supplementLikeRepository;
    private final SupplementRepository supplementRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeToggleResponseDto toggleLike(Long supplementId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


        Supplement supplement = supplementRepository.findById(supplementId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUPPLEMENT_NOT_FOUND));


        return supplementLikeRepository.findByUserAndSupplement(user, supplement)
                .map(existingSupplementLike -> {
                    supplementLikeRepository.delete(existingSupplementLike);
                    return LikeToggleResponseDto.builder()
                            .supplementId(supplementId)
                            .liked(false)
                            .build();
                })
                .orElseGet(() -> {
                    supplementLikeRepository.save(SupplementLike.builder()
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