package com.vitacheck.Notification.controller;

import com.vitacheck.user.notification.dto.NotificationSettingsDto;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.common.CustomResponse;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.Notification.service.NotificationScheduler;
import com.vitacheck.user.notification.service.NotificationSettingsService;
import com.vitacheck.common.security.AuthenticatedUser;
import com.vitacheck.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "notification-settings", description = "사용자 알림 설정 API")
@RestController
@RequestMapping("/api/v1/notification-settings")
@RequiredArgsConstructor
public class NotificationSettingsController {

    private final NotificationSettingsService notificationSettingsService;
    private final UserService userService;
    private final NotificationScheduler notificationScheduler;

    @GetMapping("/me")
    @Operation(summary = "내 알림 설정 조회", description = "현재 로그인한 사용자의 모든 알림 수신 동의 여부를 조회합니다.")
    public CustomResponse<List<NotificationSettingsDto>> getMyNotificationSettings(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        if (user == null) { throw new CustomException(ErrorCode.UNAUTHORIZED); }
        List<NotificationSettingsDto> response = notificationSettingsService.getNotificationSettings(user.getUserId()); // ◀◀ user.getId() 사용
        return CustomResponse.ok(response);
    }

    @PatchMapping("/me")
    @Operation(summary = "내 알림 설정 변경",
            description = """
                특정 알림(종류/채널)의 수신 동의 여부를 변경(토글)합니다.
                
                이벤트 및 혜택 type : "EVENT"  /  섭취 알림 type : "INTAKE"
                
                이메일 channel : "EMAIL"      /   앱 푸시 channel : "PUSH"
            """)
    public CustomResponse<String> updateMyNotificationSetting(
            @AuthenticationPrincipal AuthenticatedUser user,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = """
                            변경할 알림의 종류, 채널, 그리고 활성화 여부를 전달합니다.
                            """,
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = NotificationSettingsDto.UpdateRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "이벤트 및 혜택 이메일 알림 켜기/끄기",
                                            value = "{\"type\": \"EVENT\", \"channel\": \"EMAIL\", \"isEnabled\": false}",
                                            summary = "이벤트 및 혜택 이메일 알림"
                                    ),
                                    @ExampleObject(
                                            name = "이벤트 및 혜택 앱 푸시 알림 켜기/끄기",
                                            value = "{\"type\": \"EVENT\", \"channel\": \"PUSH\", \"isEnabled\": false}",
                                            summary = "이벤트 및 혜택 앱 푸시 알림"
                                    ),
                                    @ExampleObject(
                                            name = "섭취 이메일 알림 켜기/끄기",
                                            value = "{\"type\": \"INTAKE\", \"channel\": \"EMAIL\", \"isEnabled\": false}",
                                            summary = "섭취 이메일 알림"
                                    ),
                                    @ExampleObject(
                                            name = "섭취 앱 푸시 알림 알림 켜기/끄기",
                                            value = "{\"type\": \"INTAKE\", \"channel\": \"PUSH\", \"isEnabled\": true}",
                                            summary = "섭취 앱 푸시 알림"
                                    )
                            }
                    )
            )
            @RequestBody NotificationSettingsDto.UpdateRequest request
    ) {
        if (user == null) { throw new CustomException(ErrorCode.UNAUTHORIZED); }
        notificationSettingsService.updateNotificationSetting(user.getUserId(), request); // ◀◀ user.getId() 사용
        return CustomResponse.ok("알림 설정이 변경되었습니다.");
    }

    @PostMapping("/internal/trigger-notifications")
    @Operation(summary = "내부용 알림 발송 트리거", description = "Cron Job에 의해 호출되어 알림 발송 로직을 실행합니다. (외부 노출 금지)")
    public CustomResponse<String> triggerNotifications(HttpServletRequest request) {
        // 보안을 위해 localhost에서 온 요청인지 확인합니다.
        String remoteAddr = request.getRemoteAddr();
        if (!remoteAddr.equals("127.0.0.1") && !remoteAddr.equals("0:0:0:0:0:0:0:1")) {
            // 권한 없음 에러를 반환합니다.
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        notificationScheduler.sendRoutineNotifications();
        return CustomResponse.ok("알림 발송 작업이 성공적으로 실행되었습니다.");
    }
}