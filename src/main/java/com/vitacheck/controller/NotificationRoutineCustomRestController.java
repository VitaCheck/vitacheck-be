package com.vitacheck.controller;

import com.vitacheck.dto.CustomRoutineUpsertRequestDto;
import com.vitacheck.dto.RoutineRegisterResponseDto;
import com.vitacheck.common.CustomResponse;
import com.vitacheck.service.NotificationRoutineCustomCommandService;
import com.vitacheck.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "notification-routines", description = "복용 루틴 관련 API")
@RestController
@RequestMapping("/api/v1/notifications/routines/custom")
@RequiredArgsConstructor
public class NotificationRoutineCustomRestController {

    private final NotificationRoutineCustomCommandService customService;
    private final UserService userService;

    // 생성/수정 통합
    @Operation(summary = "커스텀 영양제 루틴 등록/수정(통합)")
    @ApiResponses({ // <-- 이 부분 추가
            @ApiResponse(responseCode = "201", description = "커스텀 루틴 생성 성공"),
            @ApiResponse(responseCode = "200", description = "커스텀 루틴 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 중복된 이름)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않았거나 권한 없는 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 루틴을 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<CustomResponse<RoutineRegisterResponseDto>> upsert(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CustomRoutineUpsertRequestDto req
    ) {
        Long userId = userService.findIdByEmail(userDetails.getUsername());
        RoutineRegisterResponseDto res = customService.upsert(userId, req);

        boolean isCreate = (req.getNotificationRoutineId() == null);
        return isCreate
                ? ResponseEntity.status(HttpStatus.CREATED).body(CustomResponse.created(res))
                : ResponseEntity.ok(CustomResponse.ok(res));
    }
}