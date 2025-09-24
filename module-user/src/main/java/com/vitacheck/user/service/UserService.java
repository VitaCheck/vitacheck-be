package com.vitacheck.user.service;

import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.user.domain.User;
import com.vitacheck.user.dto.UserDto;
import com.vitacheck.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto.InfoResponse getMyInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        int age = (user.getBirthDate() != null) ? Period.between(user.getBirthDate(), LocalDate.now()).getYears() : 0;
        return new UserDto.InfoResponse(user.getEmail(), user.getNickname(), user.getFullName(), user.getProvider(), age, user.getBirthDate(), user.getPhoneNumber());
    }

    @Transactional
    public UserDto.InfoResponse updateMyInfo(String email, UserDto.UpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateInfo(request.getNickname(), request.getBirthDate(), request.getPhoneNumber());

        int age = (user.getBirthDate() != null) ? Period.between(user.getBirthDate(), LocalDate.now()).getYears() : 0;
        return new UserDto.InfoResponse(user.getEmail(), user.getNickname(), user.getFullName(), user.getProvider(), age, user.getBirthDate(), user.getPhoneNumber());
    }

    public Long findIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND))
                .getId();
    }

    @Transactional
    public void updateFcmToken(String email, String fcmToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateFcmToken(fcmToken);
    }

    @Transactional
    public String updateProfileImageUrl(String email, String newProfileUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.changeProfileUrl(newProfileUrl);
        return user.getProfileUrl();
    }

    public String getProfileImageUrlByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND))
                .getProfileUrl();
    }

    @Transactional
    public void withdrawUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.withdraw();
    }
}