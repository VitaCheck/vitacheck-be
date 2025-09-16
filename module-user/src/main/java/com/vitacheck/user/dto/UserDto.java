package com.vitacheck.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

public class UserDto {

    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {
        private String nickname;
        private LocalDate birthDate;
        private String phoneNumber;
    }

    @Getter
    @AllArgsConstructor
    public static class InfoResponse {
        private String email;
        private String nickname;
        private String fullName;
        private String provider;
        private int age;
        private LocalDate birthDate;
        private String phoneNumber;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateFcmTokenRequest {
        private String fcmToken;
    }

    @Getter
    @NoArgsConstructor
    public static class ProfileUpdateRequest {
        @NotBlank(message="프로필 이미지 URL은 비워둘 수 없습니다.")
        private String profileImageUrl;
    }

    @Getter
    @AllArgsConstructor
    public static class ProfileUpdateResponse {
        private String profileImageUrl;
    }
}