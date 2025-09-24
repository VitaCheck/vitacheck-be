package com.vitacheck;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import java.util.Arrays;
import java.util.TimeZone;

// ✅ @SpringBootApplication 하나만 남겨둡니다.
//    (basePackages를 지정하여 모든 모듈을 스캔하도록 변경)
@SpringBootApplication(scanBasePackages = "com.vitacheck")
public class VitacheckBeApplication {

    @PostConstruct
    public void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public static void main(String[] args) {
        SpringApplication.run(VitacheckBeApplication.class, args);
    }

    // ✅ [진단 코드] 애플리케이션 시작 시 로드된 모든 부품(Bean)을 출력합니다.
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            System.out.println("========================================================================");
            System.out.println("=============== 스프링 부품(Bean) 목록을 검사합니다 ===============");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);

            for (String beanName : beanNames) {
                // 우리가 만든 부품만 필터링해서 확인
                if (beanName.contains("vitacheck")) {
                    System.out.println("FOUND BEAN: " + beanName);
                }
            }

            System.out.println("======================= 검사가 완료되었습니다 =======================");
            System.out.println("========================================================================");
        };
    }
}