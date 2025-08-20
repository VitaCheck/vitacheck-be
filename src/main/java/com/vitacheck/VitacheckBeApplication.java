package com.vitacheck;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing // Auditing 기능 활성화
@EnableScheduling
public class VitacheckBeApplication {

    @PostConstruct
    public void started() {
        // 애플리케이션의 기본 시간대를 한국으로 설정합니다.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public static void main(String[] args) {
        SpringApplication.run(VitacheckBeApplication.class, args);
    }

}
