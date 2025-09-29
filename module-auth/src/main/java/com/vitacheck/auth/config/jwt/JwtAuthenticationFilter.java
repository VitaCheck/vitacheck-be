package com.vitacheck.auth.config.jwt;

import com.vitacheck.common.enums.Gender;
import com.vitacheck.common.security.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/api/v1/auth/signup",
            "/api/v1/auth/social-signup",
            "/api/v1/auth/pre-signup"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (EXCLUDE_PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ▼ 기존 필터 로직 ▼
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authorizationHeader.substring(7);

        if (jwtUtil.validateToken(token)) {
            Claims claims = jwtUtil.getClaims(token);
            Long userId = claims.get("userId", Long.class);
            String email = claims.get("email", String.class);
            Gender gender = Gender.valueOf(claims.get("gender", String.class));
            LocalDate birthDate = LocalDate.parse(claims.get("birthDate", String.class));
            String role = claims.get("role", String.class);
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    email,
                    gender,
                    birthDate,
                    authorities
            );

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    authenticatedUser,
                    "",
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        }

        filterChain.doFilter(request, response);
    }
}
