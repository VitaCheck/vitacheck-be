package com.vitacheck.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // 응답 상태 코드를 401 Unauthorized로 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 응답 컨텐츠 타입을 JSON으로 설정
        response.setContentType("application/json;charset=UTF-8");

        // 응답 바디에 담을 에러 메시지 생성
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", "인증이 필요합니다: " + authException.getMessage());

        // ObjectMapper를 사용해 Map을 JSON 문자열로 변환하고, 응답 스트림에 쓴다.
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}

