package com.vitacheck.user.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class AuthUserDto {
    private final Long id;
    private final String email;
    private final String password;
    private final String role;
    private final String gender;
    private final LocalDate birthDate;
}
