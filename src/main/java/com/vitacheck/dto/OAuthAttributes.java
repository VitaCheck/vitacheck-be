package com.vitacheck.dto;

import com.vitacheck.domain.user.Role;
import com.vitacheck.domain.user.User;
import com.vitacheck.domain.user.UserStatus;
import com.vitacheck.util.RandomNicknameGenerator;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Getter
@Data
@Builder
@NoArgsConstructor
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

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        // Naver는 응답이 response라는 키 값 내부에 중첩되어 있습니다.
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        /* 랜덤 닉네임 생성을 위한 로직(주석 or 추후 삭제)
        String name = (String) response.get("name");
        log.info("네이버에서 받은 원본 닉네임: {}", name);
        name = null;
         */

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                // .name(name) // 랜덤 닉네임 테스트용 코드
                .email((String) response.get("email"))
                .provider("naver")
                .providerId((String) response.get("id"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // 처음 가입하는 사용자일 경우, User 엔티티를 생성하는 메소드
    public User toEntity() {
        String finalNickname = this.name;

        // 닉네임이 없거나 비어있을 경우 랜덤 닉네임 생성기 호출
        if (finalNickname == null || finalNickname.isBlank()) {
            finalNickname = RandomNicknameGenerator.generate();
        }

        return User.builder()
                .nickname(finalNickname)
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .status(UserStatus.ACTIVE)
                .lastLoginAt(LocalDateTime.now())
                .role(Role.USER) // 기본 권한은 USER
                .build();
    }
}
