package com.vitacheck.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "routine_times")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutineTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_routines_id", nullable = false)
    private NotificationRoutine notificationRoutine;

    @Column(name = "time", nullable = false)
    private LocalTime time;

    public void setNotificationRoutine(NotificationRoutine routine) {
        this.notificationRoutine = routine;
    }

    @Builder
    public RoutineTime(LocalTime time) {
        this.time = time;
    }
}
