package com.vitacheck.config.oauth;

import com.vitacheck.config.jwt.JwtUtil;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.OAuthAttributes;
import com.vitacheck.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. 어떤 소셜 로그인인지 파악
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String provider = token.getAuthorizedClientRegistrationId();

        // 2. OAuthAttributes 클래스 사용 -> 사용자 정보 통일
        OAuthAttributes attributes = OAuthAttributes.of(provider, provider, oAuth2User.getAttributes());

        // 가상 이메일 추가 -> provider, providerId로 사용자 찾기
        User user = userRepository.findByProviderAndProviderId(attributes.getProvider(), attributes.getProviderId())
                .orElseGet(() -> userRepository.save(attributes.toEntity()));

        /*
        // 3. DB에서 사용자 조회 -> 없으면 새로 만들기
        User user = userRepository.findByEmail(attributes.getEmail())
                .orElseGet(() -> userRepository.save(attributes.toEntity()));
        */

        // 4. JWT 생성
        String accessToken = jwtUtil.createAccessToken(user.getEmail()); // 카카오는 가상 이메일 사용
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

        // 5. 프론트엔드로 토큰을 담아 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth-redirect")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
