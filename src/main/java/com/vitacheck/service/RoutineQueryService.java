package com.vitacheck.service;

import com.vitacheck.domain.notification.NotificationRoutine;
import com.vitacheck.dto.RoutineResponseDto;
import com.vitacheck.repository.NotificationRoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutineQueryService {

    private final NotificationRoutineRepository notificationRoutineRepository;

    public List<RoutineResponseDto> getMyRoutines(Long userId) {
        List<NotificationRoutine> routines = notificationRoutineRepository.findAllByUserId(userId);

        return routines.stream()
                .map(routine -> RoutineResponseDto.builder()
                        .routineId(routine.getId())
                        .supplementId(routine.getSupplement().getId())
                        .supplementName(routine.getSupplement().getName())
                        .supplementImageUrl(routine.getSupplement().getImageUrl())
                        .daysOfWeek(routine.getRoutineDays().stream()
                                .map(day -> day.getDayOfWeek().name()) // "MON", "WED", ...
                                .collect(Collectors.toList()))
                        .times(routine.getRoutineTimes().stream()
                                .map(time -> time.getTime().toString()) // "08:00"
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }
}
