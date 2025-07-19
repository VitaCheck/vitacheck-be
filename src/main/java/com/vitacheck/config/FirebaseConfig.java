package com.vitacheck.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.vitacheck.global.apiPayload.CustomException;
import com.vitacheck.global.apiPayload.code.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Configuration
@Profile("!test")
public class FirebaseConfig {

    // prod 환경에서 사용
    @Value("${firebase.key-json:#{null}}")
    private String fcmKeyJson;

    // local 환경에서 사용
    @Value("${firebase.key-path:#{null}}")
    private String fcmKeyPath;

    // 이 Bean이 생성될 때 FirebaseApp 초기화를 먼저 수행합니다.
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        log.info("FirebaseApp 초기화를 시작합니다...");

        InputStream serviceAccount;

        // 1. prod 환경
        if (StringUtils.hasText(fcmKeyJson)) {
            log.info("Secrets Manager 키를 사용해 Firebase 초기화");
            serviceAccount = new ByteArrayInputStream(fcmKeyJson.getBytes());
        }

        // 2. local 환경
        else if (StringUtils.hasText(fcmKeyPath)) {
            log.info("로컬 파일 경로({})를 사용해 Firebase를 초기화", fcmKeyPath);
            /*
            Resource resource = new ClassPathResource(fcmKeyPath);
            if (!resource.exists()) {
                throw new CustomException(ErrorCode.FCM_FILE_NOT_FOUND);
            }

             */
            serviceAccount = new FileInputStream(fcmKeyPath);
        }

        // 3. 둘 다 없는 경우
        else {
            throw new CustomException(ErrorCode.FCM_CONFIG_NOT_FOUND);
        }

        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
        if (firebaseApps != null && !firebaseApps.isEmpty()) {
            for (FirebaseApp app : firebaseApps) {
                if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
                    log.info("FirebaseApp이 이미 초기화되었습니다.");
                    return app;
                }
            }
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp app = FirebaseApp.initializeApp(options);
        log.info("FirebaseApp 초기화 성공.");
        return app;
    }

    // 위에서 생성된 FirebaseApp Bean을 주입받아 FirebaseMessaging Bean을 생성합니다.
    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}