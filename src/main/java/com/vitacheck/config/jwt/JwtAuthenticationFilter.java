package com.vitacheck.config.jwt;

import com.vitacheck.domain.user.User;
import com.vitacheck.repository.UserRepository;
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

        // 1. 헤더에서 토큰 추출 (기존과 동일)
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request,response);
            return;
        }
        String token = authorizationHeader.substring(7);

        // 2. 토큰 검증 (기존과 동일)
        if (jwtUtil.validateToken(token)) {
            // 3. 토큰 유효 -> 사용자 정보 추출
            String email = jwtUtil.getEmailFromToken(token);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new SecurityException("User not found with email: " + email));

            // CustomUserDetails 클래스를 만들어 사용하는 것이 일반적입니다.
            UserDetails userDetails = new CustomUserDetails(user);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, // 👈 Principal로 User 엔티티 객체를 사용
                    "",
                    userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request,response);
    }
}