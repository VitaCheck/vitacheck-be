package com.vitacheck.controller;

import com.vitacheck.dto.IntakeRecordRequestDto;
import com.vitacheck.dto.IntakeRecordResponseDto;
import com.vitacheck.global.apiPayload.CustomResponse;
import com.vitacheck.service.IntakeRecordCommandService;
import com.vitacheck.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
            summary = "오늘의 섭취 여부 기록",
            description = "사용자가 특정 복용 루틴에 대해 오늘 영양제를 섭취했는지를 기록합니다. 이미 기록이 있으면 수정됩니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = IntakeRecordRequestDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "복용함",
                                            value = """
                                {
                                  "notificationRoutineId": 1,
                                  "isTaken": true
                                }
                                """
                                    ),
                                    @ExampleObject(
                                            name = "복용 안함",
                                            value = """
                                {
                                  "notificationRoutineId": 1,
                                  "isTaken": false
                                }
                                """
                                    )
                            }
                    )
            )
    )
    @PostMapping
    public CustomResponse<IntakeRecordResponseDto> recordIntake(
            @Valid @RequestBody IntakeRecordRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        Long userId = userService.findIdByEmail(email);

        IntakeRecordResponseDto response = intakeRecordCommandService.recordIntake(requestDto, userId);
        return CustomResponse.ok(response);
    }
}