package com.vitacheck.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Configuration
@Profile("!test")
public class FirebaseConfig {

    // 이 Bean이 생성될 때 FirebaseApp 초기화를 먼저 수행합니다.
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        log.info("FirebaseApp 초기화를 시작합니다...");

        ClassPathResource resource = new ClassPathResource("firebase-service-account-key.json");
        InputStream serviceAccount = resource.getInputStream();

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
    // 이렇게 하면 항상 초기화가 완료된 후에 이 메소드가 호출됩니다.
    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}