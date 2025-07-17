package com.vitacheck.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineResponseDto {

    private Long routineId;

    private Long supplementId;

    private String supplementName;

    private String supplementImageUrl;

    private List<String> daysOfWeek; // ex: ["MON", "WED", "FRI"]

    private List<String> times;      // ex: ["08:00", "20:00"]
}
