package com.vitacheck.auth.config.jwt;

import com.vitacheck.user.domain.User;
import com.vitacheck.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자의 Role을 기반으로 권한을 생성합니다.
        // "ROLE_" 접두사는 Spring Security의 규칙입니다.
        if (user.getRole() == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // Principal을 식별할 수 있는 고유한 값으로, 여기서는 이메일을 사용합니다.
        return user.getEmail();
    }
    public User getUser() {
        return this.user;
    }

    // 아래 4개는 계정의 상태를 나타냅니다. (만료, 잠김 등)
    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정이 만료되지 않았음
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정이 잠기지 않았음
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명(비밀번호)이 만료되지 않았음
    }

    @Override
    public boolean isEnabled() {
        // User 엔티티의 status 필드를 기반으로 계정 활성화 여부를 반환할 수 있습니다.
        return user.getStatus() == UserStatus.ACTIVE;
    }
}
