package com.vitacheck.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;

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
