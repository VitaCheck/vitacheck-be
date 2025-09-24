package com.vitacheck.service;

import com.vitacheck.Notification.domain.NotificationChannel;
import com.vitacheck.Notification.domain.NotificationRoutine;
import com.vitacheck.Notification.service.FcmService;
import com.vitacheck.Notification.service.NotificationScheduler;
import com.vitacheck.Intake.domain.RoutineDayOfWeek;
import com.vitacheck.Notification.domain.NotificationSettings;
import com.vitacheck.Notification.domain.NotificationType;
import com.vitacheck.Notification.repository.NotificationRoutineRepository;
import com.vitacheck.Notification.repository.NotificationSettingsRepository; // import 추가
import com.vitacheck.product.domain.Supplement.Supplement;
import com.vitacheck.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional; // import 추가

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock
    private NotificationRoutineRepository notificationRoutineRepository;

    @Mock
    private FcmService fcmService;

    // 👇👇👇 1. 가짜 Repository Mock 객체를 추가합니다. 👇👇👇
    @Mock
    private NotificationSettingsRepository notificationSettingsRepository;

    @InjectMocks
    private NotificationScheduler notificationScheduler;

    private User testUser;
    private Supplement testSupplement;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fcmToken("test_fcm_token_12345")
                .build();

        testSupplement = Supplement.builder()
                .id(10L)
                .name("비타민C 1000")
                .build();
    }

    @Test
    @DisplayName("스케줄러가 현재 시간에 맞는 루틴을 찾아 알림을 성공적으로 발송한다")
    void sendRoutineNotifications_Success() {
        // given: 준비
        NotificationRoutine routine = NotificationRoutine.builder()
                .user(testUser)
                .supplement(testSupplement)
                .build();

        // 👇👇👇 2. 알림 설정 시나리오를 추가합니다. 👇👇👇
        // "이 사용자는 INTAKE/PUSH 알림을 받겠다고(isEnabled=true) 설정했습니다" 라고 가정
        NotificationSettings settings = NotificationSettings.builder().isEnabled(true).build();
        when(notificationSettingsRepository.findByUserAndTypeAndChannel(
                any(User.class),
                eq(NotificationType.INTAKE),
                eq(NotificationChannel.PUSH)
        )).thenReturn(Optional.of(settings));

        // 기존 시나리오
        when(notificationRoutineRepository.findRoutinesToSend(
                any(RoutineDayOfWeek.class),
                any(LocalTime.class)
        )).thenReturn(List.of(routine));

        // when: 실행
        notificationScheduler.sendRoutineNotifications();

        // then: 검증
        verify(fcmService, times(1)).sendNotification(
                eq("test_fcm_token_12345"),
                eq("💊 영양제 복용 시간입니다!"),
                eq("'비타민C 1000'를 복용할 시간이에요.")
        );
    }
}