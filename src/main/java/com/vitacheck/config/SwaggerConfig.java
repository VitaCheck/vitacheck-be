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
        // API 문서에 대한 기본 정보 설정
        Info info = new Info()
                .title("💊VitaCheck API")
                .version("1.0.0")
                .description("비타체크 API 명세서입니다.");

        // JWT 인증 스키마 설정
        String jwtSchemeName = "JWT Authentication";
        SecurityScheme securityScheme = new SecurityScheme()
                .name(jwtSchemeName)
                .type(SecurityScheme.Type.HTTP) // HTTP 타입
                .scheme("bearer") // Bearer 토큰 방식 사용
                .bearerFormat("JWT"); // 토큰 형식 지정

        Components components = new Components().addSecuritySchemes(jwtSchemeName, securityScheme);

        return new OpenAPI()
                .info(info)
                .components(components);
    }

    @Bean
    public OperationCustomizer customOperationCustomizer() {
        // 이 커스터마이저가 각 API(@Operation)를 돌면서 설정을 변경합니다.
        return (operation, handlerMethod) -> {
            // @AuthenticationPrincipal 어노테이션이 있는지 확인합니다.
            boolean isAuthRequired = Arrays.stream(handlerMethod.getMethodParameters())
                    .anyMatch(param -> param.getParameterAnnotation(AuthenticationPrincipal.class) != null);

            // 어노테이션이 있다면 해당 API에만 자물쇠 아이콘을 추가합니다.
            if (isAuthRequired) {
                operation.addSecurityItem(new SecurityRequirement().addList("JWT Authentication"));
            }
            return operation;
        };
    }
}