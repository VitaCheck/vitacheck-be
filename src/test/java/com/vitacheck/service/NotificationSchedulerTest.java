package com.vitacheck.service;

import com.vitacheck.domain.notification.NotificationChannel;
import com.vitacheck.domain.notification.NotificationRoutine;
import com.vitacheck.domain.RoutineDayOfWeek;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.notification.NotificationSettings;
import com.vitacheck.domain.notification.NotificationType;
import com.vitacheck.domain.user.User;
import com.vitacheck.domain.user.UserDevice;
import com.vitacheck.repository.NotificationRoutineRepository;
import com.vitacheck.repository.NotificationSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock
    private NotificationRoutineRepository notificationRoutineRepository;

    @Mock
    private FcmService fcmService;

    @Mock
    private NotificationSettingsRepository notificationSettingsRepository;

    @InjectMocks
    private NotificationScheduler notificationScheduler;

    private User testUser;
    private Supplement testSupplement;

    @BeforeEach
    void setUp() {
        // 1. 토큰 없이 테스트용 User 객체를 생성합니다.
        testUser = User.builder()
                .id(1L)
                .build();

        // 2. 토큰을 가진 UserDevice 객체를 생성하고 User와 연결합니다.
        UserDevice testDevice = UserDevice.builder()
                .fcmToken("test_fcm_token_12345")
                .user(testUser)
                .build();

        // 3. User의 기기 목록에 생성한 기기를 추가합니다.
        testUser.getDevices().add(testDevice);

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

        NotificationSettings settings = NotificationSettings.builder().isEnabled(true).build();
        when(notificationSettingsRepository.findByUserAndTypeAndChannel(
                any(User.class),
                eq(NotificationType.INTAKE),
                eq(NotificationChannel.PUSH)
        )).thenReturn(Optional.of(settings));

        when(notificationRoutineRepository.findRoutinesToSend(
                any(RoutineDayOfWeek.class),
                any(LocalTime.class)
        )).thenReturn(List.of(routine));

        // when: 실행
        notificationScheduler.sendRoutineNotifications();

        // then: 검증
        // ✅ 이제 sendMulticastNotification 메소드가 토큰 리스트와 함께 호출되는지 확인합니다.
        verify(fcmService, times(1)).sendMulticastNotification(
                eq(List.of("test_fcm_token_12345")),
                eq("💊 영양제 복용 시간입니다!"),
                eq("'비타민C 1000'를 복용할 시간이에요.")
        );
    }
}