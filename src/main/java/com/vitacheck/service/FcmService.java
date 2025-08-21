package com.vitacheck.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;

    public void sendMulticastNotification(List<String> tokens, String title, String body) {
        if (tokens == null || tokens.isEmpty()) {
            log.warn("FCM 토큰 목록이 비어있어 알림을 보낼 수 없습니다.");
            return;
        }

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(notification)
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendMulticast(message);
            log.info("FCM 멀티캐스트 알림 발송 성공: {} messages", response.getSuccessCount());
            if (response.getFailureCount() > 0) {
                log.warn("FCM 멀티캐스트 알림 발송 일부 실패: {} messages", response.getFailureCount());
            }
        } catch (FirebaseMessagingException e) {
            log.error("FCM 멀티캐스트 알림 발송 실패: {}", e.getMessage());
        }
    }

    public void sendNotification(String token, String title, String body) {
        if (token == null || token.isEmpty()) {
            log.warn("FCM 토큰이 비어있어 알림을 보낼 수 없습니다.");
            return;
        }

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .build();

        try {
            firebaseMessaging.send(message);
            log.info("FCM 알림 발송 성공: To = {}, Title = {}", token, title);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 알림 발송 실패: {}", e.getMessage());
        }
    }
}
