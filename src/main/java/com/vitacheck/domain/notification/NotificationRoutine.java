package com.vitacheck.domain.notification;

import com.vitacheck.domain.common.BaseTimeEntity;
import com.vitacheck.domain.RoutineDay;
import com.vitacheck.domain.RoutineTime;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notification_routines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationRoutine extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplement_id", nullable = false)
    private Supplement supplement;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @OneToMany(mappedBy = "notificationRoutine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutineDay> routineDays = new ArrayList<>();

    @OneToMany(mappedBy = "notificationRoutine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutineTime> routineTimes = new ArrayList<>();

    @Builder
    public NotificationRoutine(User user, Supplement supplement) {
        this.user = user;
        this.supplement = supplement;
        this.isEnabled = true;
    }

    public void addRoutineDay(RoutineDay day) {
        this.routineDays.add(day);
        day.setNotificationRoutine(this);
    }

    public void addRoutineTime(RoutineTime time) {
        this.routineTimes.add(time);
        time.setNotificationRoutine(this);
    }
}

