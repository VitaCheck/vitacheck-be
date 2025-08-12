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

    // 네이버로부터 추가로 받을 수 있는 정보 필드
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
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .provider("google")
                .providerId((String) attributes.get("sub"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    /**
     * 네이버 API 응답 명세에 맞춰 사용자 정보를 파싱합니다.
     * @param userNameAttributeName 네이버에서 고유 사용자 ID를 나타내는 속성 키 ("id")
     * @param attributes OAuth2User의 attribute 맵
     * @return 파싱된 사용자 정보가 담긴 OAuthAttributes 객체
     */
    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        // API 응답에서 "response" 필드에 실제 사용자 정보가 포함되어 있습니다.
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        // 생년월일 파싱 (null 및 공백 체크)
        // 'birthyear'와 'birthday'를 조합하여 LocalDate 객체를 생성합니다.
        LocalDate parsedBirthDate = null;
        String birthYear = (String) response.get("birthyear");
        String birthday = (String) response.get("birthday"); // "MM-DD" 형식
        if (birthYear != null && !birthYear.isBlank() && birthday != null && !birthday.isBlank()) {
            try {
                parsedBirthDate = LocalDate.parse(birthYear + "-" + birthday, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                log.error("네이버 생년월일 파싱 중 오류가 발생했습니다. birthYear={}, birthday={}", birthYear, birthday, e);
            }
        }

        // 성별 파싱 (null 체크)
        // 'F'는 FEMALE, 'M'은 MALE로 변환합니다.
        Gender parsedGender = null;
        String naverGender = (String) response.get("gender");
        if (naverGender != null) {
            if ("F".equalsIgnoreCase(naverGender)) {
                parsedGender = Gender.FEMALE;
            } else if ("M".equalsIgnoreCase(naverGender)) {
                parsedGender = Gender.MALE;
            }
        }

        // 휴대폰 번호 파싱 (null 체크 및 포맷팅)
        // '-' 문자를 제거하여 숫자만 저장합니다.
        String mobile = (String) response.get("mobile");
        String parsedPhoneNumber = (mobile != null) ? mobile.replaceAll("-", "") : null;

        return OAuthAttributes.builder()
                .name((String) response.get("name")) // 실명
                .email((String) response.get("email")) // 이메일
                .provider("naver")
                .providerId((String) response.get("id")) // 고유 식별자
                .phoneNumber(parsedPhoneNumber)
                .birthDate(parsedBirthDate)
                .gender(parsedGender)
                .attributes(response) // 사용자 정보 전체
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public User toEntity() {
        String finalNickname = this.name;
        if (finalNickname == null || finalNickname.isBlank()) {
            finalNickname = RandomNicknameGenerator.generate();
        }

        return User.builder()
                .nickname(finalNickname)
                .fullName(this.name)
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .gender(gender)
                .birthDate(birthDate)
                .phoneNumber(phoneNumber)
                .status(UserStatus.ACTIVE)
                .lastLoginAt(LocalDateTime.now())
                .role(Role.USER)
                .build();
    }
}