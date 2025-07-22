package com.vitacheck.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.Optional;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("💊VitaCheck API")
                .version("1.0.0")
                .description("비타체크 API 명세서입니다.");

        // API 문서에 인증 기능 추가
        String jwtSchemeName = "JWT Authentication"; // 문서상 보여질 이름

        // 1. SecurityScheme 설정
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Authorization") // ✅ 실제 HTTP 헤더 이름
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        // 2. SecurityRequirement 설정
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        // 3. Components에 SecurityScheme 추가
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, securityScheme);

        // 4. OpenAPI 객체에 Components와 SecurityRequirement 추가
        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement) // ✅ 모든 API에 전역적으로 인증 적용
                .components(components);
    }
}