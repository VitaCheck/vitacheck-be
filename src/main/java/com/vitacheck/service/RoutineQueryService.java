package com.vitacheck.service;

import com.vitacheck.domain.IntakeRecord;
import com.vitacheck.domain.notification.NotificationRoutine;
import com.vitacheck.dto.RoutineResponseDto;
import com.vitacheck.repository.IntakeRecordRepository;
import com.vitacheck.repository.NotificationRoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutineQueryService {

    private final NotificationRoutineRepository notificationRoutineRepository;
    private final IntakeRecordRepository intakeRecordRepository;

    public List<RoutineResponseDto> getMyRoutines(Long userId, LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        List<NotificationRoutine> routines = notificationRoutineRepository.findAllByUserId(userId);

        return routines.stream()
                .map(routine -> {
                    boolean isTaken = intakeRecordRepository
                            .findByUserAndNotificationRoutineAndDate(routine.getUser(), routine, targetDate)
                            .map(IntakeRecord::getIsTaken)
                            .orElse(false);


                    return RoutineResponseDto.builder()
                            .notificationRoutineId(routine.getId())
                            .supplementId(routine.getSupplement().getId())
                            .supplementName(routine.getSupplement().getName())
                            .supplementImageUrl(routine.getSupplement().getImageUrl())
                            .daysOfWeek(routine.getRoutineDays().stream()
                                    .map(day -> day.getDayOfWeek().name())
                                    .collect(Collectors.toList()))
                            .times(routine.getRoutineTimes().stream()
                                    .map(time -> time.getTime().toString())
                                    .collect(Collectors.toList()))
                            .isTaken(isTaken)
                            .build();
                })
                .collect(Collectors.toList());
    }

}
