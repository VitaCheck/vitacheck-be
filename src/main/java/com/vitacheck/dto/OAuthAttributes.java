package com.vitacheck.dto;

import com.vitacheck.domain.user.Role;
import com.vitacheck.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String provider;
    private String providerId;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String provider, String providerId) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver("id", attributes);
        }
        if ("kakao".equals(registrationId)) {
            return ofKakao("id", attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .provider("google")
                .providerId((String) attributes.get("sub"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        // Kakao는 응답이 중첩된 JSON 구조이므로, 필요한 정보를 꺼내야 합니다.
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
        String providerId = String.valueOf(attributes.get("id"));

        String email = null;
        if (kakaoAccount != null) {
            email = (String) kakaoAccount.get("email");
        }
        // 가상 이메일 생성
        if (email == null || email.isBlank()) {
            email = providerId + "@kakao.com";
        }

        return OAuthAttributes.builder()
                .name((String) kakaoProfile.get("nickname"))
                .email(email)
                .provider("kakao")
                .providerId(providerId)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        // Naver는 응답이 response라는 키 값 내부에 중첩되어 있습니다.
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .provider("naver")
                .providerId((String) response.get("id"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // 처음 가입하는 사용자일 경우, User 엔티티를 생성하는 메소드
    public User toEntity() {
        return User.builder()
                .nickname(name)
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .role(Role.USER) // 기본 권한은 USER
                .build();
    }
}
