package com.vitacheck.user.dto;

import com.vitacheck.common.enums.Gender;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CreateUserRequest {
    // 자체 회원가입 정보
    private final String email;
    private final String password;
    private final String fullName;
    private final String nickname;
    private final Gender gender;
    private final LocalDate birthDate;
    private final String phoneNumber;

    // 소셜 회원가입 정보
    private final String provider;
    private final String providerId;
    private final String fcmToken;

    // 공통
    private final List<Long> agreedTermIds;
}