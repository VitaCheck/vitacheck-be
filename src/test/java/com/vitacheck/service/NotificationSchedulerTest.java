package com.vitacheck.service;

import com.vitacheck.domain.notification.NotificationRoutine;
import com.vitacheck.domain.RoutineDayOfWeek;
import com.vitacheck.domain.Supplement;
import com.vitacheck.domain.user.User;
import com.vitacheck.repository.NotificationRoutineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock
    private NotificationRoutineRepository notificationRoutineRepository;

    @Mock
    private FcmService fcmService;

    @InjectMocks
    private NotificationScheduler notificationScheduler;

    private User testUser;
    private Supplement testSupplement;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 가짜 유저와 영양제 객체 생성
        testUser = User.builder()
                .id(1L)
                .fcmToken("test_fcm_token_12345") // FCM 토큰 설정
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
        // 1. 테스트용 가짜 루틴 객체 생성
        NotificationRoutine routine = NotificationRoutine.builder()
                .user(testUser)
                .supplement(testSupplement)
                .build();

        // 2. Repository가 특정 요일과 시간을 받으면 위에서 만든 가짜 루틴을 반환하도록 설정
        when(notificationRoutineRepository.findRoutinesToSend(
                any(RoutineDayOfWeek.class), // 어떤 요일이든
                any(LocalTime.class)      // 어떤 시간이든
        )).thenReturn(List.of(routine));

        // when: 실행
        // 스케줄러의 public 메소드를 직접 호출
        notificationScheduler.sendRoutineNotifications();

        // then: 검증
        // FcmService의 sendNotification 메소드가 정확히 1번 호출되었는지 확인
        // 그리고 어떤 파라미터로 호출되었는지 검증
        verify(fcmService, times(1)).sendNotification(
                eq("test_fcm_token_12345"),                  // FCM 토큰이 올바른가?
                eq("💊 영양제 복용 시간입니다!"),                // 제목이 올바른가?
                eq("'비타민C 1000'를 복용할 시간이에요.")       // 본문이 올바른가?
        );
    }
}