package com.vitacheck.auth.dto;

import com.vitacheck.common.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;


public class AuthDto {
    @Getter
    @NoArgsConstructor
    @Schema(description = "1단계 임시 회원가입 요청 DTO")
    public static class PreSignUpRequest {
        @Schema(description = "사용자 이메일 주소", example = "user@vitacheck.com")
        private String email;
        @Schema(description = "사용자 비밀번호 (8자 이상)", example = "password123!")
        private String password;
        @Schema(description = "사용할 닉네임", example = "행복한쿼카")
        private String nickname;
        @NotEmpty
        @Schema(description = "사용자가 동의한 약관의 ID 목록", example = "[1, 2, 3]")
        private List<Long> agreedTermIds;
    }

    // 자체 회원가입 요청 DTO
    @Getter
    @NoArgsConstructor
    @Schema(description = "2단계 최종 회원가입 요청 DTO")
    public static class SignUpRequest {
        @Schema(description = "사용자 실명", example = "홍길동")
        private String fullName;
        @Schema(description = "성별", example = "MALE")
        private Gender gender;
        @Schema(description = "생년월일", example = "2000-01-01")
        private LocalDate birthDate;
        @Schema(description = "휴대폰 번호", example = "010-1234-5678")
        private String phoneNumber;
    }

    // 자체 로그인 요청 DTO
    @Getter
    @NoArgsConstructor
    @Schema(description = "자체 로그인 요청 DTO")
    public static class LoginRequest {
        @Schema(description = "이메일 주소", example = "user@vitacheck.com")
        private String email;

        @Schema(description = "비밀번호", example = "password123!")
        private String password;
    }

    @Getter
    @NoArgsConstructor
    public static class SocialSignUpRequest {
        // 소셜 로그인 제공 정보
        private String email;
        private String fullName;
        private String provider;
        private String providerId;

        // 사용자가 직접 입력한 정보
        private String nickname;
        private Gender gender;
        private LocalDate birthDate;
        private String phoneNumber;
        private String fcmToken;

        public SocialSignUpRequest(OAuthAttributes attributes) {
            this.email = attributes.getEmail();
            this.fullName = attributes.getName();
            this.provider = attributes.getProvider();
            this.providerId = attributes.getProviderId();
            this.gender = attributes.getGender();
            this.birthDate = attributes.getBirthDate();
            this.phoneNumber = attributes.getPhoneNumber();
        }
    }

    // JWT 토큰 응답 DTO
    @Getter
    @AllArgsConstructor
    @Schema(description = "로그인 성공 시 JWT 토큰 응답 DTO")
    public static class TokenResponse {
        @Schema(description = "API 접근을 위한 Access Token")
        private String accessToken;

        @Schema(description = "Access Token 재발급을 위한 Refresh Token")
        private String refreshToken;
    }

    @Getter
    @NoArgsConstructor
    public static class RefreshTokenRequest {
        private String refreshToken;
    }
}
