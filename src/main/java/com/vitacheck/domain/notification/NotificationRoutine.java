package com.vitacheck.domain.notification;

import com.vitacheck.domain.CustomSupplement;
import com.vitacheck.domain.IntakeRecord;
import com.vitacheck.domain.RoutineDetail;
import com.vitacheck.domain.common.BaseTimeEntity;
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
    @JoinColumn(name = "supplement_id")
    private Supplement supplement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_supplement_id")
    private CustomSupplement customSupplement;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @OneToMany(mappedBy = "notificationRoutine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutineDetail> routineDetails = new ArrayList<>();

    @OneToMany(mappedBy = "notificationRoutine", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<IntakeRecord> intakeRecords = new ArrayList<>();

    @Builder
    public NotificationRoutine(User user, Supplement supplement) {
        this.user = user;
        this.supplement = supplement;
        this.isEnabled = true;
    }

    public void addRoutineDetail(RoutineDetail detail) {
        this.routineDetails.add(detail);
        detail.setNotificationRoutine(this);
    }

    public void clearRoutineDetails() {
        this.routineDetails.clear();
    }

    public boolean isOwner(Long userId) {
        return this.user != null && this.user.getId().equals(userId);
    }

    public boolean isCustom() {
        return this.customSupplement != null;
    }

    public void linkCustom(CustomSupplement cs) {
        this.customSupplement = cs;
        this.supplement = null;
    }

    public void linkCatalog(Supplement s) {
        this.supplement = s;
        this.customSupplement = null;
    }

    public static NotificationRoutine forCustom(User user, CustomSupplement cs) {
        NotificationRoutine r = NotificationRoutine.builder()
                .user(user)
                .supplement(null)
                .build();
        r.customSupplement = cs;
        return r;
    }

    public static NotificationRoutine forCatalog(User user, Supplement s) {
        NotificationRoutine r = NotificationRoutine.builder()
                .user(user)
                .supplement(s)
                .build();
        r.customSupplement = null;
        return r;
    }
}

