package com.vitacheck.domain.notification;

import com.vitacheck.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_settings")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; // 알림 종류 (EVENT, INTAKE)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel; // 알림 채널 (EMAIL, SMS, PUSH)

    @Column(nullable = false)
    private boolean isEnabled; // 수신 동의 여부 (true/false)

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
