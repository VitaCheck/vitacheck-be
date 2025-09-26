package com.vitacheck.auth.config.jwt;

import com.vitacheck.common.security.UserContextProvider;
import com.vitacheck.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class JwtUserContextProvider implements UserContextProvider {

    @Override
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return (user != null) ? user.getId() : null;
    }

    @Override
    public String getCurrentEmail() {
        User user = getCurrentUser();
        return (user != null) ? user.getEmail() : null;
    }

    @Override
    public com.vitacheck.common.enums.Gender getCurrentGender() {
        User user = getCurrentUser();
        return (user != null) ? user.getGender() : null;
    }

    @Override
    public LocalDate getCurrentBirthDate() {
        User user = getCurrentUser();
        return (user != null) ? user.getBirthDate() : null;
    }

    @Override
    public Integer getCurrentAge() {
        User user = getCurrentUser();
        if (user == null || user.getBirthDate() == null) {
            return null;
        }
        return java.time.Period.between(user.getBirthDate(), java.time.LocalDate.now()).getYears();
    }

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return null;
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getUser();
    }
}

