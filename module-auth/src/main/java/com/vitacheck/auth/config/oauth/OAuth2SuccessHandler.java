package com.vitacheck.auth.config.oauth;

import com.vitacheck.auth.config.jwt.JwtUtil;
import com.vitacheck.auth.dto.AuthUserDto;
import com.vitacheck.auth.dto.OAuthAttributes;
import com.vitacheck.auth.service.provider.AuthUserProvider; // provider를 import 합니다.
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    // 1. UserRepository를 AuthUserProvider로 교체합니다.
    private final AuthUserProvider authUserProvider;
    private final JwtUtil jwtUtil;

    @Value("${oauth2.redirect.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String provider = token.getAuthorizedClientRegistrationId();

        OAuthAttributes attributes = OAuthAttributes.of(provider, provider, oAuth2User.getAttributes());

        // 2. provider를 사용하여 이메일로 사용자를 찾습니다.
        Optional<AuthUserDto> userOptional = authUserProvider.findByEmail(attributes.getEmail());

        String targetUrl;

        if (userOptional.isEmpty()) {
            // 경우 1: 신규 사용자 -> 임시 토큰과 함께 소셜 회원가입 페이지로 리디렉션
            log.info("신규 사용자입니다. 추가 정보 입력 페이지로 리디렉션합니다.");

            String tempToken = jwtUtil.createTempSocialToken(attributes);
            targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/social-signup")
                    .queryParam("tempToken", tempToken)
                    .build()
                    .toUriString();
        } else {
            // 경우 2: 기존 사용자 -> JWT를 발급하고 메인 앱으로 리디렉션
            log.info("기존 사용자입니다. JWT 발급 후 메인 페이지로 이동합니다.");
            AuthUserDto user = userOptional.get();

            // 3. AuthUserDto를 사용하여 Access Token을 생성합니다.
            String accessToken = jwtUtil.createAccessToken(user);
            String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

            targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-redirect")
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .queryParam("fcmUpdateRequired", "true")
                    .build()
                    .toUriString();
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}