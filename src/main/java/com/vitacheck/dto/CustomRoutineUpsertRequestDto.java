package com.vitacheck.dto;

import com.vitacheck.domain.RoutineDayOfWeek;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomRoutineUpsertRequestDto {
    private Long notificationRoutineId;

    @NotBlank
    private String name;

    @Pattern(regexp="^$|^https?://.*", message="imageUrl은 빈값 또는 http(s) URL 형식")
    private String imageUrl;

    @NotEmpty
    @jakarta.validation.Valid
    private List<Scheduled> schedules;

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Scheduled {

        @jakarta.validation.constraints.NotNull
        private RoutineDayOfWeek dayOfWeek;

        @jakarta.validation.constraints.NotNull
        @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "HH:mm")
        @io.swagger.v3.oas.annotations.media.Schema(
                type = "string",
                format = "time",
                example = "08:00",
                description = "24시간 포맷 HH:mm"
        )
        private java.time.LocalTime time;
    }
}
