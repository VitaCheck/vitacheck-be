package com.vitacheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing // Auditing 기능 활성화
@EnableScheduling
public class VitacheckBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(VitacheckBeApplication.class, args);
    }

}
