package com.vitacheck.service;

import com.vitacheck.domain.NotificationRoutine;
import com.vitacheck.domain.RoutineDayOfWeek;
import com.vitacheck.repository.NotificationRoutineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationRoutineRepository notificationRoutineRepository;
    private final FcmService fcmService;

    @Scheduled(cron = "0 * * * * *") // 매분 0초에 실행
    public void sendRoutineNotifications() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek currentDay = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime().withSecond(0).withNano(0);

        // 👇👇👇 여기가 수정된 요일 변환 로직입니다! 👇👇👇
        RoutineDayOfWeek routineDay = convertToRoutineDayOfWeek(currentDay);
        if (routineDay == null) {
            log.warn("오늘은 알림을 보내는 요일이 아닙니다: {}", currentDay);
            return;
        }

        log.info("{} {}시 {}분에 발송될 알림을 찾습니다.", routineDay, currentTime.getHour(), currentTime.getMinute());

        List<NotificationRoutine> routines = notificationRoutineRepository.findRoutinesToSend(
                routineDay,
                currentTime
        );

        if (routines.isEmpty()) {
            log.info("발송할 알림이 없습니다.");
            return;
        }

        for (NotificationRoutine routine : routines) {
            String fcmToken = routine.getUser().getFcmToken();
            String title = "💊 영양제 복용 시간입니다!";
            String body = String.format("'%s'를 복용할 시간이에요.", routine.getSupplement().getName());
            fcmService.sendNotification(fcmToken, title, body);
        }
    }

    // 👇👇👇 요일 변환을 위한 헬퍼 메소드 추가 👇👇👇
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