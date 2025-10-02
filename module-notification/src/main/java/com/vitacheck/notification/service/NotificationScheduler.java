package com.vitacheck.notification.service;

import com.vitacheck.notification.domain.RoutineDayOfWeek;
import com.vitacheck.notification.domain.NotificationChannel;
import com.vitacheck.notification.domain.NotificationRoutine;
import com.vitacheck.notification.domain.NotificationSettings;
import com.vitacheck.notification.domain.NotificationType;
import com.vitacheck.notification.repository.NotificationRoutineRepository;
import com.vitacheck.notification.repository.NotificationSettingsRepository; // Repository 주입
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

        if (routineDay == null) return;

        log.info("{} {}시 {}분에 발송될 알림을 찾습니다.", routineDay, currentTime.getHour(), currentTime.getMinute());
        List<NotificationRoutine> routines = notificationRoutineRepository.findRoutinesToSend(routineDay, currentTime);

        if (routines.isEmpty()) {
            log.info("발송할 알림이 없습니다.");
            return;
        }

        for (NotificationRoutine routine : routines) {
            Optional<NotificationSettings> setting = notificationSettingsRepository.findByUserAndTypeAndChannel(
                    routine.getUser(),
                    NotificationType.INTAKE,
                    NotificationChannel.PUSH
            );

            // 설정이 존재하고, isEnabled가 true일 때만 알림 발송
            if (setting.isPresent() && setting.get().isEnabled()) {
                String fcmToken = routine.getUser().getFcmToken();
                String title = "💊 영양제 복용 시간입니다!";

                String supplementName;
                if (routine.isCustom()) {
                    supplementName = routine.getCustomSupplement().getName();
                } else {
                    supplementName = routine.getSupplement().getName();
                }

                String body = String.format("'%s'를 복용할 시간이에요.", supplementName);
                fcmService.sendNotification(fcmToken, title, body);
            } else {
                log.info("사용자 ID: {}님이 섭취 푸시 알림을 꺼두어 발송하지 않았습니다.", routine.getUser().getId());
            }
        }
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