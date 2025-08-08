package com.vitacheck.controller;

import com.vitacheck.dto.NotificationSettingsDto;
import com.vitacheck.global.apiPayload.CustomResponse;
import com.vitacheck.service.NotificationSettingsService;
import com.vitacheck.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "notification-settings", description = "사용자 알림 설정 API")
@RestController
@RequestMapping("/api/v1/notification-settings")
@RequiredArgsConstructor
public class NotificationSettingsController {

    private final NotificationSettingsService notificationSettingsService;
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 알림 설정 조회", description = "현재 로그인한 사용자의 모든 알림 수신 동의 여부를 조회합니다.")
    public CustomResponse<List<NotificationSettingsDto>> getMyNotificationSettings(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = userService.findIdByEmail(userDetails.getUsername());
        List<NotificationSettingsDto> response = notificationSettingsService.getNotificationSettings(userId);
        return CustomResponse.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "내 알림 설정 변경", description = "특정 알림(종류/채널)의 수신 동의 여부를 변경(토글)합니다.")
    public CustomResponse<String> updateMyNotificationSetting(
            @AuthenticationPrincipal UserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "변경할 알림의 종류, 채널, 그리고 활성화 여부를 전달합니다.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = NotificationSettingsDto.UpdateRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "섭취 알림 끄기",
                                            value = "{\"type\": \"INTAKE\", \"channel\": \"PUSH\", \"isEnabled\": false}",
                                            summary = "섭취 푸시 알림 끄기 예시"
                                    ),
                                    @ExampleObject(
                                            name = "이벤트 알림 켜기",
                                            value = "{\"type\": \"EVENT\", \"channel\": \"SMS\", \"isEnabled\": true}",
                                            summary = "이벤트 SMS 알림 켜기 예시"
                                    )
                            }
                    )
            )
            @RequestBody NotificationSettingsDto.UpdateRequest request
    ) {
        Long userId = userService.findIdByEmail(userDetails.getUsername());
        notificationSettingsService.updateNotificationSetting(userId, request);
        return CustomResponse.ok("알림 설정이 변경되었습니다.");
    }
}