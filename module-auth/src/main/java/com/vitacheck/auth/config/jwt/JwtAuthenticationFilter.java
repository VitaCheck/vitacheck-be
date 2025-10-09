package com.vitacheck.auth.config.jwt;

import com.vitacheck.common.enums.Gender;
import com.vitacheck.common.security.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // ◀◀ Slf4j 어노테이션 추가
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Slf4j 
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // --- 1. 요청 경로와 헤더 정보 로깅 ---
        log.info(">>>>> JWT Filter started for request: {} {}", request.getMethod(), request.getRequestURI());

        final String authorizationHeader = request.getHeader("Authorization");

        // --- 2. 토큰 존재 여부 확인 ---
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn(">>>>> No JWT token found in request header. Passing to next filter.");
            filterChain.doFilter(request, response);
            return;
        }

        // --- 3. 토큰 추출 및 유효성 검증 ---
        try {
            String token = authorizationHeader.substring(7);
            log.info(">>>>> Token found: {}", token);

            if (jwtUtil.validateToken(token)) {
                log.info(">>>>> Token is valid. Proceeding with authentication.");

                // --- 4. 토큰에서 사용자 정보 추출 및 인증 객체 생성 ---
                Claims claims = jwtUtil.getClaims(token);
                Long userId = claims.get("userId", Long.class);
                String email = claims.get("email", String.class);
                Gender gender = Gender.valueOf(claims.get("gender", String.class));
                LocalDate birthDate = LocalDate.parse(claims.get("birthDate", String.class));
                String role = claims.get("role", String.class);
                List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

                AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                        userId, email, gender, birthDate, authorities
                );

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        authenticatedUser, "", authorities
                );

                // --- 5. SecurityContext에 인증 정보 저장 ---
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info(">>>>> Authentication successful for user: {}. SecurityContext updated.", email);

            } else {
                log.warn(">>>>> Token is invalid.");
            }
        } catch (Exception e) {
            log.error(">>>>> An error occurred during JWT processing: {}", e.getMessage(), e);
        }

        // --- 6. 다음 필터로 요청 전달 ---
        log.info(">>>>> JWT Filter finished. Passing to next filter.");
        filterChain.doFilter(request, response);
    }
}