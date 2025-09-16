package com.vitacheck.auth.config.jwt;

import com.vitacheck.user.domain.User;
import com.vitacheck.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // ▼ [신규 추가] 필터가 무시해야 할 경로 목록 ▼
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/api/v1/auth/signup",
            "/api/v1/auth/social-signup",
            "/api/v1/auth/pre-signup"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // ▼ [신규 추가] 현재 요청 경로가 무시 목록에 포함되면, 필터 로직을 건너뜀 ▼
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
            String email = claims.get("email", String.class);

            if (claims.get("provider") == null) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new SecurityException("User not found with email: " + email));

                UserDetails userDetails = new CustomUserDetails(user);

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        "",
                        userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
