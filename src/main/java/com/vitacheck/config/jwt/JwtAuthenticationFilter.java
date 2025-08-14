package com.vitacheck.config.jwt;

import com.vitacheck.domain.user.User;
import com.vitacheck.repository.UserRepository;
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

@Component // ✅ 3. 스프링 빈으로 등록
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authorizationHeader.substring(7);

        if (jwtUtil.validateToken(token)) {
            Claims claims = jwtUtil.getClaims(token);
            String email = claims.get("email", String.class);

            // 'provider' 클레임이 없으면 일반 Access Token으로 간주하고 사용자 조회
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
            // 'provider' 클레임이 있으면 임시 소셜 토큰이므로, DB 조회 없이 통과시킵니다.
            // 어차피 이후 socialSignUp API에서 해당 토큰을 다시 검증하고 사용자를 생성하게 됩니다.
        }

        filterChain.doFilter(request, response);
    }
}