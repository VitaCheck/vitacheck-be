package com.vitacheck.controller;

import com.vitacheck.dto.IntakeRecordResponseDto;
import com.vitacheck.global.apiPayload.CustomResponse;
import com.vitacheck.service.IntakeRecordCommandService;
import com.vitacheck.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications/records")
@RequiredArgsConstructor
@Tag(name = "intake-records", description = "오늘의 섭취 여부 기록 API")
public class IntakeRecordController {

    private final IntakeRecordCommandService intakeRecordCommandService;
    private final UserService userService;

    @Operation(
            summary = "오늘의 섭취 여부 토글",
            description = "사용자가 특정 복용 루틴에 대해 오늘 영양제를 섭취했는지를 토글합니다. 최초 등록 시 true, 다시 누르면 false로 변경됩니다.",
            parameters = {
                    @Parameter(
                            name = "notificationRoutineId",
                            description = "복용 루틴 ID",
                            required = true,
                            example = "1"
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
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        Long userId = userService.findIdByEmail(email);

        IntakeRecordResponseDto response = intakeRecordCommandService.toggleIntake(notificationRoutineId, userId);
        return CustomResponse.ok(response);
    }
}