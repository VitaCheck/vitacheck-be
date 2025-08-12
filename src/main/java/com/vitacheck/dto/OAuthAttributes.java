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

    // 새로운 정보를 담을 필드
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
        // 구글 로직은 그대로 유지
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .provider("google")
                .providerId((String) attributes.get("sub"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // 👇👇👇 ofNaver 메소드를 아래의 안전한 코드로 교체합니다. 👇👇👇
    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        // [수정] 생년월일 파싱 - null 체크 추가
        LocalDate parsedBirthDate = null;
        String birthYear = (String) response.get("birthyear");
        String birthday = (String) response.get("birthday"); // "MM-dd"
        if (birthYear != null && !birthYear.isBlank() && birthday != null && !birthday.isBlank()) {
            try {
                parsedBirthDate = LocalDate.parse(birthYear + "-" + birthday, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                log.error("네이버 생년월일 파싱 중 오류 발생", e);
            }
        }

        // [수정] 성별 파싱 - null 체크 추가
        Gender parsedGender = null;
        String naverGender = (String) response.get("gender");
        if (naverGender != null) {
            if ("F".equalsIgnoreCase(naverGender)) {
                parsedGender = Gender.FEMALE;
            } else if ("M".equalsIgnoreCase(naverGender)) {
                parsedGender = Gender.MALE;
            }
        }

        // [수정] 전화번호 - null 체크 추가 (네이버는 mobile 필드로 제공)
        String mobile = (String) response.get("mobile");
        String phoneNumber = (mobile != null) ? mobile.replaceAll("-", "") : null;

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .provider("naver")
                .providerId((String) response.get("id"))
                .phoneNumber(phoneNumber)
                .birthDate(parsedBirthDate)
                .gender(parsedGender)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // toEntity 메소드는 소셜 로그인 후 '추가 정보 입력' 단계에서 사용되므로,
    // 여기서는 수정하지 않아도 됩니다. (UserService의 socialSignUp이 이 역할을 담당)
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