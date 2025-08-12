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


                    // 👇👇👇 DTO를 생성하는 builder 부분을 아래 코드로 교체해주세요. 👇👇👇

                    // 1. routineDetails 리스트를 ScheduleResponse DTO 리스트로 변환
                    List<RoutineResponseDto.ScheduleResponse> scheduleResponses = routine.getRoutineDetails().stream()
                            .map(detail -> RoutineResponseDto.ScheduleResponse.builder()
                                    .dayOfWeek(detail.getDayOfWeek())
                                    .time(detail.getTime())
                                    .build())
                            .collect(Collectors.toList());

                    // 2. 변환된 리스트를 포함하여 최종 DTO 생성
                    return RoutineResponseDto.builder()
                            .notificationRoutineId(routine.getId())
                            .supplementId(routine.getSupplement().getId())
                            .supplementName(routine.getSupplement().getName())
                            .supplementImageUrl(routine.getSupplement().getImageUrl())
                            .schedules(scheduleResponses) // 수정된 부분
                            .isTaken(isTaken)
                            .build();
                })
                .collect(Collectors.toList());
    }

}
