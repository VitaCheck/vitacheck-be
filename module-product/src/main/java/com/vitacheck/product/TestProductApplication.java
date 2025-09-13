package com.vitacheck.product;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "com.vitacheck")
public class TestProductApplication {

    @PostConstruct
    public void started() {
        // 애플리케이션의 기본 시간대를 한국으로 설정합니다.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public static void main(String[] args) {
        SpringApplication.run(TestProductApplication.class, args);
    }

}
