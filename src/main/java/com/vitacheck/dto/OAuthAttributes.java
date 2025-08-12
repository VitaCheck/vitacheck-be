package com.vitacheck.dto;

import com.vitacheck.domain.user.Gender;
import com.vitacheck.domain.user.Role;
import com.vitacheck.domain.user.User;
import com.vitacheck.domain.user.UserStatus;
import com.vitacheck.util.RandomNicknameGenerator;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    // 👇👇👇 1. 새로운 정보를 담을 필드를 추가합니다. 👇👇👇
    private Gender gender;
    private LocalDate birthDate;
    private String phoneNumber;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String provider, String providerId, Gender gender, LocalDate birthDate, String phoneNumber) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.gender = gender;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver("id", attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        // 구글은 기본 정보만 제공하므로 기존 로직 유지
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .provider("google")
                .providerId((String) attributes.get("sub"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // 👇👇👇 2. ofNaver 메소드를 아래와 같이 수정합니다. 👇👇👇
    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        // 생년월일 파싱 (YYYY-MM-DD 형식으로 조합)
        String birthYear = (String) response.get("birthyear");
        String birthday = (String) response.get("birthday"); // "MM-dd" 형식
        LocalDate birthDate = null;
        if (birthYear != null && birthday != null) {
            birthDate = LocalDate.parse(birthYear + "-" + birthday, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        // 성별 파싱 (F/M -> FEMALE/MALE)
        Gender gender = null;
        String naverGender = (String) response.get("gender");
        if ("F".equalsIgnoreCase(naverGender)) {
            gender = Gender.FEMALE;
        } else if ("M".equalsIgnoreCase(naverGender)) {
            gender = Gender.MALE;
        }

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .provider("naver")
                .providerId((String) response.get("id"))
                .phoneNumber((String) response.get("mobile")) // 휴대폰 번호 추가
                .birthDate(birthDate) // 생년월일 추가
                .gender(gender) // 성별 추가
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // 👇👇👇 3. toEntity 메소드를 아래와 같이 수정합니다. 👇👇👇
    public User toEntity() {
        String finalNickname = this.name;
        if (finalNickname == null || finalNickname.isBlank()) {
            finalNickname = RandomNicknameGenerator.generate();
        }

        return User.builder()
                .nickname(finalNickname)
                .fullName(this.name) // 실명 정보 추가
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .gender(gender) // 성별 정보 추가
                .birthDate(birthDate) // 생년월일 정보 추가
                .phoneNumber(phoneNumber) // 휴대폰 번호 정보 추가
                .status(UserStatus.ACTIVE)
                .lastLoginAt(LocalDateTime.now())
                .role(Role.USER)
                .build();
    }
}