package com.vitacheck.dto;

import com.vitacheck.domain.RoutineDayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineResponseDto {

    private Long notificationRoutineId;

    private Long supplementId;

    private String supplementName;

    private String supplementImageUrl;

    private boolean isTaken;

    private List<ScheduleResponse> schedules;

    @Getter
    @Builder
    public static class ScheduleResponse {
        private RoutineDayOfWeek dayOfWeek;
        private LocalTime time;
    }
}
