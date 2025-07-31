package com.vitacheck.config;

import com.vitacheck.config.jwt.JwtAuthenticationFilter;
import com.vitacheck.config.jwt.JwtUtil;
import com.vitacheck.config.oAuth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer; // 이 부분을 추가해주세요.
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtUtil jwtUtil;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] PERMIT_ALL_URL_ARRAY = {
            // Swagger UI v3
            "/v3/api-docs/**",
            "/swagger-ui/**",
            // login, signup
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
            // OAuth2
            "/",
            "/login/**",
            "/oauth2/**",
            // 기타
            "/error",
            "/fcm_test.html",
            "/firebase-messaging-sw.js",
            //성분
            "/api/v1/purposes/**",
            "/api/v1/supplements/**",
            "/api/v1/combinations/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS 정책 설정 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프론트엔드 개발 서버 주소 허용
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 모든 헤더 허용
        configuration.setAllowedHeaders(List.of("*"));

        // 인증 정보(쿠키, 인증 헤더)를 포함한 요청 허용
        configuration.setAllowCredentials(true);

        // 브라우저에 노출할 헤더 설정
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 위 설정 적용
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 기본 설정: CSRF, HTTP Basic, Form Login, Session 비활성화
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // CORS 설정 적용 (수정된 부분)
        http.cors(Customizer.withDefaults());

        // 요청 경로별 권한 설정
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL_URL_ARRAY).permitAll()
                        .anyRequest().authenticated());

        // OAuth2 로그인 설정
        http
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler));

        // JWT 필터 및 예외 처리 설정
        http
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(customAuthenticationEntryPoint));

        return http.build();
    }
}