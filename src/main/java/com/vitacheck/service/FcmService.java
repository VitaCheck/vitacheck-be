package com.vitacheck.service;

import com.google.firebase.messaging.*;
import com.vitacheck.repository.UserDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final UserDeviceRepository userDeviceRepository;

    public void sendMulticastNotification(List<String> tokens, String title, String body) {
        if (tokens == null || tokens.isEmpty()) {
            log.warn("FCM tokens is empty. Skip sending.");
            return;
        }

        // 권장: 300~500 단위로 쪼개서 발송
        final int CHUNK = 400;
        List<List<String>> chunks = chunk(tokens, CHUNK);

        for (List<String> batch : chunks) {
            try {
                // WebPush 노티 포함(브라우저에서 보장된 표시)
                WebpushConfig webpush = WebpushConfig.builder()
                        .setNotification(new WebpushNotification(title, body))
                        .build();

                MulticastMessage msg = MulticastMessage.builder()
                        .addAllTokens(batch)
                        .putData("type", "INTAKE")
                        .setWebpushConfig(webpush)
                        .build();

                // ✅ 핵심: /batch 안 쓰는 경로
                BatchResponse br = FirebaseMessaging.getInstance().sendEachForMulticast(msg);

                handleBatchResponse(batch, br);

                log.info("FCM multicast sent. success={}, failure={}",
                        br.getSuccessCount(), br.getFailureCount());

            } catch (Exception e) {
                // 네트워크 등 일반 예외 방어
                log.error("FCM multicast unexpected error: {}", e.getMessage(), e);
            }

            // (선택) 너무 큰 묶음 연속 발송시 약간의 간격을 둘 수도 있음
            sleepQuietly(Duration.ofMillis(50));
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
            FirebaseMessaging.getInstance().send(message);
            log.info("FCM 알림 발송 성공: To = {}, Title = {}", token, title);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 알림 발송 실패: {}", e.getMessage());
        }
    }

    private void handleBatchResponse(List<String> batchTokens, BatchResponse br) {
        List<SendResponse> responses = br.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse r = responses.get(i);
            String t = batchTokens.get(i);

            if (!r.isSuccessful()) {
                FirebaseMessagingException e = r.getException();
                String code = (e != null && e.getMessagingErrorCode() != null)
                        ? e.getMessagingErrorCode().name() : "UNKNOWN";
                log.warn("FCM 실패 token={} code={} msg={}", t, code, (e != null ? e.getMessage() : ""));

                // 말소된 토큰은 DB에서 제거
                if (e != null && e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                    userDeviceRepository.deleteByFcmToken(t);
                    log.info("UNREGISTERED 토큰 정리: {}", t);
                }
            }
        }
    }

    private static List<List<String>> chunk(List<String> list, int size) {
        List<List<String>> chunks = new ArrayList<>();
        int n = list.size();
        for (int i = 0; i < n; i += size) {
            chunks.add(list.subList(i, Math.min(n, i + size)));
        }
        return chunks;
    }

    private static void sleepQuietly(Duration d) {
        try {
            Thread.sleep(d.toMillis());
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
