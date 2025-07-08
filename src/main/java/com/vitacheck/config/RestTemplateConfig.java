package com.vitacheck.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // 외부 API 쉽게 호출하기 위한 RestTemplate 생성
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
