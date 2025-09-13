package com.vitacheck.domain.user;

import com.vitacheck.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(nullable = false, name = "full_name", length = 100)
    private String fullName;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false, name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 20)
    private String provider; // 예: google, kakao

    @Column(name = "provider_id", length = 100)
    private String providerId; // 소셜 로그인 서비스 고유 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    private LocalDateTime lastLoginAt;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "fcm_token", columnDefinition = "TEXT")
    private String fcmToken;

    private String profileUrl;

    public void changeProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public User updateFromSocial(String nickname) {
        this.nickname = nickname;
        this.lastLoginAt = LocalDateTime.now();
        return this;
    }

    public void updateInfo(String nickname, LocalDate birthDate, String phoneNumber) {
        // 값이 null이 아닌 경우에만 필드를 업데이트합니다.
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
        if (birthDate != null) {
            this.birthDate = birthDate;
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            this.phoneNumber = phoneNumber;
        }
    }

    public void withdraw() {
        this.password = null;
        this.fullName = "탈퇴한사용자";
        this.nickname = "탈퇴한사용자";
        this.gender = Gender.NONE;
        this.birthDate = LocalDate.of(1900, 1, 1);
        this.phoneNumber = "010-0000-0000";
        this.providerId = null;
        this.fcmToken = null;
        this.profileUrl = null;
        this.status = UserStatus.DELETED;
    }
}