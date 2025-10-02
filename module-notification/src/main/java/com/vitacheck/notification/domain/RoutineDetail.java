package com.vitacheck.notification.domain;


import com.vitacheck.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "routine_details")
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class RoutineDetail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_routine_id", nullable = false)
    private NotificationRoutine notificationRoutine;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private RoutineDayOfWeek dayOfWeek;

    @Column(name = "time", nullable = false)
    private LocalTime time;

    public void setNotificationRoutine(NotificationRoutine routine) {
        this.notificationRoutine = routine;
    }


}
