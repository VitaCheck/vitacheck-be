package com.vitacheck.notification.dto;

import com.vitacheck.notification.domain.RoutineDayOfWeek;
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
public class RoutineRegisterResponseDto {

    private Long notificationRoutineId;

    private Long supplementId;

    private String supplementName;

    private String supplementImageUrl;

    private List<ScheduleResponse> schedules;

    private Boolean isEnabled;

    @Getter
    @Builder
    public static class ScheduleResponse {
        private RoutineDayOfWeek dayOfWeek;
        private LocalTime time;
    }
}
