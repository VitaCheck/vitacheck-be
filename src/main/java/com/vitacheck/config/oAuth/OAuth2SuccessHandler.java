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

            // ✅✅✅ 핵심 수정 부분 시작 ✅✅✅
            UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString("http://localhost:5173/social-signup")
                    .queryParam("email", attributes.getEmail())
                    .queryParam("fullName", attributes.getName())
                    .queryParam("provider", attributes.getProvider())
                    .queryParam("providerId", attributes.getProviderId());

            // 네이버에서 받은 추가 정보가 있다면 URL 파라미터에 추가
            if (attributes.getGender() != null) {
                urlBuilder.queryParam("gender", attributes.getGender().name());
            }
            if (attributes.getBirthDate() != null) {
                urlBuilder.queryParam("birthDate", attributes.getBirthDate().toString()); // yyyy-MM-dd 형식
            }
            if (attributes.getPhoneNumber() != null) {
                urlBuilder.queryParam("phoneNumber", attributes.getPhoneNumber());
            }

            targetUrl = urlBuilder.build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();
            // ✅✅✅ 핵심 수정 부분 끝 ✅✅✅
        }
        else {
            log.info("기존 사용자입니다. JWT 발급 후 메인 페이지로 이동합니다.");
            String accessToken = jwtUtil.createAccessToken(user.getEmail());
            String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

            targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth-redirect")
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .build()
                    .toUriString();
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}