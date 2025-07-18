package com.vitacheck.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class IntakeRecordRequestDto {

    @Schema(description = "복용 루틴 ID", example = "1")
    @NotNull(message = "복용 루틴 ID는 필수입니다.")
    private Long notificationRoutineId;

    @Schema(description = "복용 여부 (복용했으면 true)", example = "true")
    @NotNull(message = "복용 여부는 필수입니다.")
    private Boolean isTaken;
}