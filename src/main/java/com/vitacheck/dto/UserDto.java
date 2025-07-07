package com.vitacheck.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserDto {
    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {
        private String nickname;
    }

    @Getter
    @AllArgsConstructor
    public static class InfoResponse {
        private String email;
        private String nickname;
        private String provider;
    }
}
