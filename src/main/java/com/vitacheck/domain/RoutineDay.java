package com.vitacheck.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "routine_days")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutineDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_routines_id", nullable = false)
    private NotificationRoutine notificationRoutine;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private RoutineDayOfWeek dayOfWeek;

    public void setNotificationRoutine(NotificationRoutine routine) {
        this.notificationRoutine = routine;
    }

    @Builder
    public RoutineDay(RoutineDayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
