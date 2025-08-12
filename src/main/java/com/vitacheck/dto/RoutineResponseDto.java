package com.vitacheck.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    // 🔹 커스텀 여부 표시 (프론트 구분용)
    private Boolean isCustom;

    // 커스텀일 때는 null
    private Long supplementId;

    private String supplementName;

    private String supplementImageUrl;

    private boolean isTaken;

    private List<ScheduleResponse> schedules;

    @Getter
    @Builder
    public static class ScheduleResponse {
        private RoutineDayOfWeek dayOfWeek;

        // 🔹 Swagger/JSON에 "HH:mm"로 보이게
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        private LocalTime time;
    }
}