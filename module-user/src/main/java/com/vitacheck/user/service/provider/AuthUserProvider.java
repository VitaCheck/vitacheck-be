package com.vitacheck.user.service.provider;

import com.vitacheck.user.dto.AuthUserDto;

import java.util.Optional;

public interface AuthUserProvider {
    Optional<AuthUserDto> findByEmail(String email);
}
