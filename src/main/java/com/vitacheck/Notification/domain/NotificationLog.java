package com.vitacheck.Notification.domain;

import com.vitacheck.user.domain.User;
import com.vitacheck.user.notification.domain.NotificationChannel;
import com.vitacheck.user.notification.domain.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private NotificationType type; // EVENT, INTAKE

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel; // PUSH, SMS, EMAIL

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    private String targetUrl; // 알림 클릭 시 이동할 링크

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime sentAt; // 발송 시간
}