package com.vitacheck.notification.controller;

import com.vitacheck.notification.dto.IntakeRecordResponseDto;
import com.vitacheck.common.CustomResponse;
import com.vitacheck.notification.service.IntakeRecordCommandService;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.common.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/notifications/records")
@RequiredArgsConstructor
@Tag(name = "intake-records", description = "오늘의 섭취 여부 기록 API")
public class IntakeRecordController {

    private final IntakeRecordCommandService intakeRecordCommandService;

    @Operation(
            summary = "섭취 여부 토글",
            description = """
        사용자가 특정 복용 루틴에 대해 영양제를 섭취했는지를 토글합니다.  
        - `date`를 입력하지 않으면 오늘 날짜 기준으로 처리됩니다.  
        - `date` 형식은 `yyyy-MM-dd` (예: 2025-08-07)입니다.
        - 첫 요청 시 `true`로 생성되며, 다시 요청하면 `false`로 변경됩니다.
        """,
            parameters = {
                    @Parameter(
                            name = "notificationRoutineId",
                            description = "복용 루틴 ID",
                            required = true,
                            example = "1"
                    ),
                    @Parameter(
                            name = "date",
                            description = "기록을 토글할 날짜 (형식: yyyy-MM-dd). 입력하지 않으면 오늘 날짜가 기본값입니다.",
                            required = false,
                            example = "2025-08-07"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "토글 성공"),
                    @ApiResponse(responseCode = "404", description = "루틴이 존재하지 않음"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
            }
    )

    @PostMapping("/{notificationRoutineId}/toggle")
    public CustomResponse<IntakeRecordResponseDto> toggleIntake(
            @PathVariable Long notificationRoutineId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (user == null) { throw new CustomException(ErrorCode.UNAUTHORIZED); }
        IntakeRecordResponseDto response = intakeRecordCommandService.toggleIntake(notificationRoutineId, user.getUserId(), date); // ◀◀ user.getId() 사용
        return CustomResponse.ok(response);
    }
}