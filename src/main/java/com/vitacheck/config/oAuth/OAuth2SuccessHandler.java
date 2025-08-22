package com.vitacheck.config.oAuth;

import com.vitacheck.config.jwt.JwtUtil;
import com.vitacheck.domain.user.User;
import com.vitacheck.dto.OAuthAttributes;
import com.vitacheck.repository.UserRepository;
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
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${oauth2.redirect.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String provider = token.getAuthorizedClientRegistrationId();

        OAuthAttributes attributes = OAuthAttributes.of(provider, provider, oAuth2User.getAttributes());

        User user = userRepository.findByEmail(attributes.getEmail()).orElse(null);

        String targetUrl;

        if (user == null) {
            log.info("신규 사용자입니다. 추가 정보 입력 페이지로 리다이렉션합니다.");

            String tempToken = jwtUtil.createTempSocialToken(attributes);
            targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/social-signup")
                    .queryParam("tempToken", tempToken)
                    .build()
                    .toUriString();
        }
        else {
            log.info("기존 사용자입니다. JWT 발급 후 메인 페이지로 이동합니다.");
            String accessToken = jwtUtil.createAccessToken(user.getEmail());
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