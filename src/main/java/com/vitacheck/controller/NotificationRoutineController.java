package com.vitacheck.controller;

import com.vitacheck.dto.RoutineRegisterRequestDto;
import com.vitacheck.dto.RoutineRegisterResponseDto;
import com.vitacheck.dto.RoutineResponseDto;
import com.vitacheck.global.apiPayload.CustomResponse;
import com.vitacheck.service.NotificationRoutineCommandService;
import com.vitacheck.service.RoutineQueryService;
import com.vitacheck.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "복용 루틴 목록 조회", description = "현재 로그인한 사용자의 복용 루틴 전체 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "복용 루틴 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
    })
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
}
