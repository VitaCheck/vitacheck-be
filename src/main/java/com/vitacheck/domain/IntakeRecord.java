package com.vitacheck.domain;

import com.vitacheck.domain.notification.NotificationRoutine;
import com.vitacheck.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "intake_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IntakeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_routine_id", nullable = false)
    private NotificationRoutine notificationRoutine;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Boolean isTaken;

    @Builder
    public IntakeRecord(User user, NotificationRoutine notificationRoutine, LocalDate date, Boolean isTaken) {
        this.user = user;
        this.notificationRoutine = notificationRoutine;
        this.date = date;
        this.isTaken = isTaken;
    }

    public void updateIsTaken(Boolean isTaken) {
        this.isTaken = isTaken;
    }
}
