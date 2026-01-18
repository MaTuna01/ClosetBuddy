package io.codebuddy.userservice.domain.form.login.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException; // Spring Boot 3.x는 javax가 아니라 jakarta입니다.
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler; // <-- 이거여야 합니다!
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler { // Server...가 아닙니다.

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        // 1. 상태 코드 설정
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        // 2. 응답 타입 설정 (JSON)
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 3. 에러 메시지 Map 생성
        Map<String, Object> body = new HashMap<>();
        body.put("code", "AUTH_FAILED");
        body.put("message", exception.getMessage() != null ? exception.getMessage() : "Authentication failed");

        // 4. JSON으로 변환하여 응답 스트림에 쓰기
        objectMapper.writeValue(response.getWriter(), body);
    }
}