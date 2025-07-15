package com.vitacheck.controller;

import com.vitacheck.dto.RoutineRegisterRequestDto;
import com.vitacheck.dto.RoutineRegisterResponseDto;
import com.vitacheck.global.apiPayload.CustomResponse;
import com.vitacheck.service.NotificationRoutineCommandService;
import com.vitacheck.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "notification-routines", description = "복용 루틴 등록 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationRoutineController {

    private final NotificationRoutineCommandService notificationRoutineCommandService;
    private final UserService userService;

    @PostMapping("/routines")
    @Operation(summary = "복용 루틴 등록", description = "현재 로그인한 사용자의 영양제 복용 루틴을 등록합니다.")
    public ResponseEntity<CustomResponse<RoutineRegisterResponseDto>> registerRoutine(
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
}
