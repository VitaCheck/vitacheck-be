package com.vitacheck.auth.config.jwt;

import com.vitacheck.common.enums.Gender;
import com.vitacheck.common.security.AuthenticatedUser;
import com.vitacheck.common.security.UserContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
@RequiredArgsConstructor
public class JwtUserContextProvider implements UserContextProvider {

    @Override
    public Long getCurrentUserId() {
        return getAuthenticatedUser().getUserId();
    }

    @Override
    public String getCurrentEmail() {
        return getAuthenticatedUser().getEmail();
    }

    @Override
    public Gender getCurrentGender() {
        return getAuthenticatedUser().getGender();
    }

    @Override
    public LocalDate getCurrentBirthDate() {
        return getAuthenticatedUser().getBirthDate();
    }

    @Override
    public Integer getCurrentAge() {
        LocalDate birthDate = getAuthenticatedUser().getBirthDate();
        if (birthDate == null) {
            return null;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    private AuthenticatedUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {
            throw new IllegalStateException("User is not Authenticated");
        }

        return (AuthenticatedUser) authentication.getPrincipal();
    }
}

