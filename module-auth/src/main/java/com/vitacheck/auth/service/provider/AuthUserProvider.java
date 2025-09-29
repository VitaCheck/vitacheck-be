package com.vitacheck.auth.service.provider;

import com.vitacheck.auth.dto.AuthUserDto;

import java.util.Optional;

public interface AuthUserProvider {
    Optional<AuthUserDto> findByEmail(String email);
}
