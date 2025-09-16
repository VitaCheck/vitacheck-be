package com.vitacheck;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.vitacheck.repository", "com.vitacheck.user.repository", "com.vitacheck.auth"})
// 4. @EnableJpaAuditing은 그대로 유지합니다.
@EnableJpaAuditing
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
