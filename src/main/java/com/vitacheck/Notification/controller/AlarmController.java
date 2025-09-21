package com.vitacheck.Notification.controller;

import com.vitacheck.common.CustomResponse;
import com.vitacheck.common.code.ErrorCode;
import com.vitacheck.common.exception.CustomException;
import com.vitacheck.Notification.service.NotificationScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name="alarm", description = "알람 관련 API")
@RestController
//@RequestMapping("/api/v1/combinations")
@RequiredArgsConstructor
public class AlarmController {
    private final NotificationScheduler notificationScheduler;

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
