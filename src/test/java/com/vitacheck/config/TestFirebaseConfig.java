package com.vitacheck.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 테스트 환경에서만 사용되는 가짜 Firebase 설정입니다.
 * 실제 Firebase 초기화 없이 Mock 객체를 Bean으로 등록하여
 * 의존성 주입 에러를 방지합니다.
 */
@Configuration
@Profile("test")
public class TestFirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() {
        // Mockito를 사용해 FirebaseApp의 가짜 객체를 만듭니다.
        return Mockito.mock(FirebaseApp.class);
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        // Mockito를 사용해 FirebaseMessaging의 가짜 객체를 만듭니다.
        return Mockito.mock(FirebaseMessaging.class);
    }
}