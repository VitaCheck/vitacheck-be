package com.vitacheck.common.security;

import com.vitacheck.common.enums.Gender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDate;
import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class AuthenticatedUser {
    private final Long userId;
    private final String email;
    private final Gender gender;
    private final LocalDate birthDate;
    private final Collection<? extends GrantedAuthority> authorities;

}
