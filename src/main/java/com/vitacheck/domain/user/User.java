package com.vitacheck.domain.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    private String provider; // 예: google, kakao
    private String providerId; // 소셜 로그인 서비스 고유 ID

    @Enumerated(EnumType.STRING)
    private Role role;

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
