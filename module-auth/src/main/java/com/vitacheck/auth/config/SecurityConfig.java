package com.vitacheck.auth.config;

import com.vitacheck.auth.config.jwt.JwtAuthenticationFilter;
import com.vitacheck.auth.config.jwt.JwtUtil;
import com.vitacheck.auth.config.oauth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
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
            "/swagger-ui.html",
            // login, signup
            "/api/v1/auth/**",
            // OAuth2
            "/",
            "/login/**",
            "/oauth2/**",
            // 기타 공개 API
            "/error",
            "/fcm_test.html",
            "/firebase-messaging-sw.js",
            "/api/v1/ingredients/**",
            "/api/v1/purposes/**",
            "/api/v1/supplements/**",
            "/api/v1/combinations/**",
            "/api/v1/terms",
            "/api/v1/search/**",
            "/internal/trigger-notifications",
            "/health",
            "/api/v1/test"
    };

    private static final String[] AUTHENTICATED_URL_ARRAY = {
            "/api/v1/users/me/**",
            "/api/v1/likes/**",
            "/api/v1/notifications/**",
            "/api/v1/terms/agreements",
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
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "https://vita-check.com", "https://vitachecking.com"));

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
    @Order(1)
    public SecurityFilterChain jwtAuthenticationSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                // AntPathRequestMatcher를 사용하여 인증이 필요한 경로들을 명시적으로 지정합니다.
                .securityMatcher(AUTHENTICATED_URL_ARRAY)
                .authorizeHttpRequests(auth -> auth.requestMatchers(AUTHENTICATED_URL_ARRAY).authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // JWT 인증 필터를 추가합니다.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 인증 실패 시 처리할 EntryPoint를 설정합니다.
                .exceptionHandling(exception -> exception.authenticationEntryPoint(customAuthenticationEntryPoint));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 기본 설정: CSRF, HTTP Basic, Form Login, Session 비활성화
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // CORS 설정 적용
        http.cors(Customizer.withDefaults());

        // 요청 경로별 권한 설정
        http
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(PERMIT_ALL_URL_ARRAY).permitAll()
                );

        // OAuth2 로그인 설정
        http
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler));

        http
                .exceptionHandling(exception -> exception.authenticationEntryPoint(customAuthenticationEntryPoint));


        return http.build();
    }
}