package com.vitacheck.dto;

import com.vitacheck.domain.RoutineDayOfWeek;
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

    @Schema(description = "복용할 영양제 ID", example = "1")
    private Long supplementId;

    @NotEmpty
    @Schema(description = "복용 요일 리스트 (Enum)", example = "[\"MON\", \"WED\", \"FRI\"]")
    private List<RoutineDayOfWeek> daysOfWeek;

    @NotEmpty
    @Schema(description = "복용 시간 리스트", example = "[\"08:00\", \"20:00\"]")
    private List<LocalTime> times;
}
