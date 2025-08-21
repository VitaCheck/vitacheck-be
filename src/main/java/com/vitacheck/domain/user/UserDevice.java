package com.vitacheck.domain.user;

import com.vitacheck.domain.common.BaseTimeEntity;
import com.vitacheck.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_devices")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDevice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "fcm_token", nullable = false, unique = true, columnDefinition = "TEXT")
    private String fcmToken;

    // 기기 타입을 저장할 필드 (선택 사항이지만 권장)
    @Column(name = "device_type", length = 50)
    private String deviceType; // 예: "WEB", "IOS", "ANDROID"

    public void updateUser(User user) {
        this.user = user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}