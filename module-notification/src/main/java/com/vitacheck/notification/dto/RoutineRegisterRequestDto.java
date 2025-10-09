package com.vitacheck.notification.dto;

import com.vitacheck.notification.domain.RoutineDayOfWeek;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoutineRegisterRequestDto {

    @Schema(description = "루틴 ID (수정 시에만 전달)", example = "5")
    private Long notificationRoutineId;

    @Schema(description = "복용할 영양제 ID", example = "1")
    private Long supplementId;

    @Schema(description = "영양제 이름 (프론트에서 표시용 또는 커스텀 이름)", example = "락토핏")
    private String supplementName;

    @Schema(description = "영양제 이미지 URL", example = "https://cdn.example.com/supplement.png")
    private String supplementImageUrl;

    @NotEmpty
    private List<ScheduledRequest> schedules;

    @Getter
    public static class ScheduledRequest {
        private RoutineDayOfWeek dayOfWeek;;
        private LocalTime time;
    }
}
