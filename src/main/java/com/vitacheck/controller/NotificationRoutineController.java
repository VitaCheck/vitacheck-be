package com.vitacheck.controller;

import com.vitacheck.dto.RoutineRegisterRequestDto;
import com.vitacheck.dto.RoutineRegisterResponseDto;
import com.vitacheck.dto.RoutineResponseDto;
import com.vitacheck.common.CustomResponse;
import com.vitacheck.service.NotificationRoutineCommandService;
import com.vitacheck.service.RoutineQueryService;
import com.vitacheck.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "notification-routines", description = "복용 루틴 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationRoutineController {

    private final NotificationRoutineCommandService notificationRoutineCommandService;
    private final RoutineQueryService routineQueryService;
    private final UserService userService;

    @PostMapping("/routines")
    @Operation(
            summary = "복용 루틴 등록/수정",
            description = """
        현재 로그인한 사용자의 복용 루틴을 등록하거나 수정합니다.
        - `notificationRoutineId`가 **없으면 등록**,
        - `notificationRoutineId`가 **있으면 해당 루틴을 수정**합니다.
        - schedules 리스트에는 요일과 시간을 조합하여 복합적인 스케줄을 설정할 수 있습니다.
        """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "복용 루틴 등록 또는 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (중복 루틴, 필드 오류 등)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 루틴을 찾을 수 없음")
    })
    public ResponseEntity<CustomResponse<RoutineRegisterResponseDto>> registerOrUpdateRoutine(
            @AuthenticationPrincipal UserDetails userDetails,

            // Swagger 예시를 직접 지정하는 어노테이션 추가
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "등록 또는 수정할 루틴 정보를 전달합니다.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RoutineRegisterRequestDto.class),
                            examples = @ExampleObject(
                                    name = "복합 스케줄 등록 예시",
                                    summary = "평일 오전 9시, 주말 오후 10시에 알림 설정",
                                    value = """
                                {
                                  "supplementId": 1,
                                  "schedules": [
                                    { "dayOfWeek": "MON", "time": "09:00" },
                                    { "dayOfWeek": "TUE", "time": "09:00" },
                                    { "dayOfWeek": "WED", "time": "09:00" },
                                    { "dayOfWeek": "THU", "time": "09:00" },
                                    { "dayOfWeek": "FRI", "time": "09:00" },
                                    { "dayOfWeek": "SAT", "time": "22:00" },
                                    { "dayOfWeek": "SUN", "time": "22:00" }
                                  ]
                                }
                                """
                            )
                    )
            )
            @RequestBody @Valid RoutineRegisterRequestDto request
    ) {
        String email = userDetails.getUsername();
        Long userId = userService.findIdByEmail(email);

        RoutineRegisterResponseDto response = notificationRoutineCommandService.registerRoutine(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomResponse.created(response));
    }

    @GetMapping("/routines")
    @Operation(
            summary = "복용 루틴 목록 조회",
            description = """
        현재 로그인한 사용자의 복용 루틴 목록을 조회합니다.
        
        ✅ `date` 파라미터 관련  
        - `date`를 입력하지 않으면 **오늘 날짜(LocalDate.now())** 기준으로 조회됩니다.  
        - 날짜 형식은 `yyyy-MM-dd` (예: 2025-08-07)입니다.
        
        ✅ `isTaken` 필드 설명  
        - 각 루틴의 `isTaken` 값은 해당 날짜에 등록된 섭취 기록의 `isTaken` 필드를 기준으로 판단됩니다.  
        - 즉, 단순히 기록이 있는지 여부가 아닌 실제 저장된 `true` / `false` 값이 반영됩니다.
        """,
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(
                            name = "date",
                            description = "조회할 날짜 (형식: yyyy-MM-dd). 입력하지 않으면 오늘 날짜 기준으로 조회됩니다.",
                            required = false,
                            example = "2025-08-07"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "복용 루틴 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
            }
    )
    public ResponseEntity<CustomResponse<List<RoutineResponseDto>>> getMyRoutines(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String email = userDetails.getUsername();
        Long userId = userService.findIdByEmail(email);

        List<RoutineResponseDto> response = routineQueryService.getMyRoutines(userId, date);
        return ResponseEntity.ok(CustomResponse.ok(response));
    }

    @DeleteMapping("/routines/{notificationRoutineId}")
    @Operation(summary = "복용 루틴 삭제", description = "현재 로그인한 사용자의 특정 복용 루틴을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "복용 루틴 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "루틴을 찾을 수 없음")
    })
    public ResponseEntity<CustomResponse<Void>> deleteRoutine(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("notificationRoutineId") Long routineId
    ) {
        String email = userDetails.getUsername();
        Long userId = userService.findIdByEmail(email);

        notificationRoutineCommandService.deleteRoutine(userId, routineId);
        return ResponseEntity.ok(CustomResponse.ok(null));
    }

    @PatchMapping("/routines/{notificationRoutineId}/toggle")
    @Operation(summary = "복용 루틴 알림 ON/OFF 토글", description = "특정 복용 루틴의 알림 활성화 상태(isEnabled)를 토글합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "루틴을 찾을 수 없음")
    })
    public CustomResponse<RoutineResponseDto> toggleRoutineStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("notificationRoutineId") Long routineId
    ) {
        Long userId = userService.findIdByEmail(userDetails.getUsername());
        RoutineResponseDto response = notificationRoutineCommandService.toggleRoutine(userId, routineId);
        return CustomResponse.ok(response);
    }
}
