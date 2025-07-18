package com.vitacheck.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class IntakeRecordResponseDto {

    private Long recordId;

    private Long notificationRoutineId;

    private Boolean isTaken;

    private LocalDate date;
}