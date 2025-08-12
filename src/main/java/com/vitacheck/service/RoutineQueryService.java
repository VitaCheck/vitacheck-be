package com.vitacheck.service;

import com.vitacheck.domain.IntakeRecord;
import com.vitacheck.domain.notification.NotificationRoutine;
import com.vitacheck.dto.RoutineResponseDto;
import com.vitacheck.repository.IntakeRecordRepository;
import com.vitacheck.repository.NotificationRoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutineQueryService {

    private final NotificationRoutineRepository notificationRoutineRepository;
    private final IntakeRecordRepository intakeRecordRepository;

    public List<RoutineResponseDto> getMyRoutines(Long userId, LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        // ✅ 커스텀/카탈로그 모두 포함해서 fetch
        List<NotificationRoutine> routines = notificationRoutineRepository.findAllWithTargetsByUserId(userId);

        return routines.stream()
                .map(routine -> {
                    boolean isTaken = intakeRecordRepository
                            .findByUserAndNotificationRoutineAndDate(routine.getUser(), routine, targetDate)
                            .map(IntakeRecord::getIsTaken)
                            .orElse(false);

                    boolean isCustom = routine.isCustom();

                    Long supplementId = isCustom ? null
                            : (routine.getSupplement() != null ? routine.getSupplement().getId() : null);
                    String supplementName = isCustom
                            ? (routine.getCustomSupplement() != null ? routine.getCustomSupplement().getName() : null)
                            : (routine.getSupplement() != null ? routine.getSupplement().getName() : null);
                    String supplementImageUrl = isCustom
                            ? (routine.getCustomSupplement() != null ? routine.getCustomSupplement().getImageUrl() : null)
                            : (routine.getSupplement() != null ? routine.getSupplement().getImageUrl() : null);

                    var scheduleResponses = routine.getRoutineDetails().stream()
                            .map(detail -> RoutineResponseDto.ScheduleResponse.builder()
                                    .dayOfWeek(detail.getDayOfWeek())
                                    .time(detail.getTime())
                                    .build())
                            .toList();

                    return RoutineResponseDto.builder()
                            .notificationRoutineId(routine.getId())
                            .isCustom(isCustom)                  // 🔹 추가
                            .supplementId(supplementId)          // 커스텀은 null
                            .supplementName(supplementName)
                            .supplementImageUrl(supplementImageUrl)
                            .isTaken(isTaken)
                            .schedules(scheduleResponses)
                            .build();
                })
                .toList();
    }
}