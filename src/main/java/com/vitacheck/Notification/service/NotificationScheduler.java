package com.vitacheck.Notification.service;

import com.vitacheck.Intake.domain.RoutineDayOfWeek;
import com.vitacheck.user.domain.notification.NotificationChannel;
import com.vitacheck.Notification.domain.NotificationRoutine;
import com.vitacheck.user.domain.notification.NotificationSettings;
import com.vitacheck.user.domain.notification.NotificationType;
import com.vitacheck.Notification.repository.NotificationRoutineRepository;
import com.vitacheck.user.repository.NotificationSettingsRepository; // Repository 주입
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationRoutineRepository notificationRoutineRepository;
    private final NotificationSettingsRepository notificationSettingsRepository; // 주입
    private final FcmService fcmService;

   //@Scheduled(cron = "0 * * * * *")
    public void sendRoutineNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime().withSecond(0).withNano(0);
        RoutineDayOfWeek routineDay = convertToRoutineDayOfWeek(now.getDayOfWeek());

        log.info("===== NotificationScheduler 시작 =====");
        log.info("현재 서버 시간: {}", now);
        log.info("알림 검색 기준 시간 (초/나노초 제외): {}", currentTime);
        log.info("알림 검색 기준 요일: {}", routineDay);

        if (routineDay == null) {
            log.warn("유효하지 않은 요일({})이므로 스케줄러를 종료합니다.", now.getDayOfWeek());
            log.info("===== NotificationScheduler 종료 =====");
            return;
        }

        log.info("{} {}시 {}분에 발송될 알림을 찾습니다.", routineDay, currentTime.getHour(), currentTime.getMinute());
        List<NotificationRoutine> routines;
        try {
            routines = notificationRoutineRepository.findRoutinesToSend(routineDay, currentTime);
            log.info("DB 조회 결과: {}개의 발송 대상 루틴을 찾았습니다.", routines.size());
        } catch (Exception e) {
            log.error("findRoutinesToSend 쿼리 실행 중 오류 발생", e);
            log.info("===== NotificationScheduler 종료 (DB 오류) =====");
            return; // DB 오류 시 더 이상 진행하지 않음
        }

        if (routines.isEmpty()) {
            log.info("발송할 알림이 없습니다.");
            log.info("===== NotificationScheduler 종료 =====");
            return;
        }

        for (NotificationRoutine routine : routines) {
            Long userId = routine.getUser() != null ? routine.getUser().getId() : null;
            Long routineId = routine.getId();
            log.info("--- 루틴 ID: {}, 사용자 ID: {} 처리 시작 ---", routineId, userId);

            if (userId == null) {
                log.warn("루틴 ID {}에 연결된 사용자 정보가 없습니다. 건너<0xEB><0x9A><0xA5>니다.", routineId);
                continue;
            }

            Optional<NotificationSettings> settingOptional;
            try {
                settingOptional = notificationSettingsRepository.findByUserAndTypeAndChannel(
                        routine.getUser(),
                        NotificationType.INTAKE,
                        NotificationChannel.PUSH
                );
            } catch (Exception e) {
                log.error("사용자 ID {}의 알림 설정 조회 중 오류 발생 (루틴 ID: {})", userId, routineId, e);
                continue; // 설정 조회 실패 시 해당 루틴 건너<0xEB><0x9A><0xA5>
            }

            boolean isPresent = settingOptional.isPresent();
            boolean isEnabled = isPresent && settingOptional.get().isEnabled();
            log.info("사용자 ID {}의 섭취 푸시(INTAKE/PUSH) 설정: 존재 여부={}, 활성화 여부={}", userId, isPresent, isEnabled);
            // 설정이 존재하고, isEnabled가 true일 때만 알림 발송
            if (isPresent && isEnabled) {
                String fcmToken = routine.getUser().getFcmToken();
                log.info("사용자 ID {}의 FCM 토큰: {}", userId, (fcmToken != null && !fcmToken.isEmpty()) ? "[존재]" : "[없음 또는 비어있음]");
                if (fcmToken == null || fcmToken.isEmpty()) {
                    log.warn("사용자 ID {}의 FCM 토큰이 없어 알림을 발송할 수 없습니다. (루틴 ID: {})", userId, routineId);
                    continue; // 토큰 없으면 다음 루틴으로
                }


                String title = "💊 영양제 복용 시간입니다!";
                String supplementName;
                if (routine.isCustom() && routine.getCustomSupplement() != null) {
                    supplementName = routine.getCustomSupplement().getName();
                } else if (!routine.isCustom() && routine.getSupplement() != null) {
                    supplementName = routine.getSupplement().getName();
                } else {
                    supplementName = "[알 수 없는 영양제]"; // 예외 케이스 처리
                    log.warn("루틴 ID {}의 영양제 정보(커스텀/카탈로그)를 찾을 수 없습니다.", routineId);
                }

                String body = String.format("'%s'를 복용할 시간이에요.", supplementName);

                log.info("FCM 발송 시도: To={}, Title={}, Body={}", fcmToken, title, body);

                fcmService.sendNotification(fcmToken, title, body);
            } else {
                log.info("사용자 ID: {}님이 섭취 푸시 알림을 꺼두어 발송하지 않았습니다.", routine.getUser().getId());
            }
            log.info("--- 루틴 ID: {} 처리 완료 ---", routineId);
        }
        log.info("===== NotificationScheduler 종료 =====");
    }

    private RoutineDayOfWeek convertToRoutineDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> RoutineDayOfWeek.MON;
            case TUESDAY -> RoutineDayOfWeek.TUE;
            case WEDNESDAY -> RoutineDayOfWeek.WED;
            case THURSDAY -> RoutineDayOfWeek.THU;
            case FRIDAY -> RoutineDayOfWeek.FRI;
            case SATURDAY -> RoutineDayOfWeek.SAT;
            case SUNDAY -> RoutineDayOfWeek.SUN;
        };
    }
}