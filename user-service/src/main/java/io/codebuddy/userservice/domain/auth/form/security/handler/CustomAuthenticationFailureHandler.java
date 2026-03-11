package io.codebuddy.userservice.domain.auth.form.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        // 클라이언트 정보 추출
        String memberId = request.getParameter("memberId");  // form login 파라미터
        String ip = getClientIp(request);


        // 1. 상태 코드 설정
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        // 2. 응답 타입 설정 (JSON)
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String code, message;

        // 보안상 "아이디/비번 틀림" + "존재하지 않음"은 항상 동일 메시지
        if (exception instanceof BadCredentialsException ||
                exception instanceof UsernameNotFoundException) {
            code = "INVALID_CREDENTIALS";
            message = "아이디 또는 비밀번호가 올바르지 않습니다.";

            log.info("Login failed: memberId={}, IP={}",
                    memberId, ip);

            // 서버 문제(로그는 남기되 클라이언트엔 모호하게)
        } else {
            code = "AUTH_FAILED";
            message = "로그인 처리 중 오류가 발생했습니다.";
            log.warn("Auth failure [{}]: memberId={}, IP={}, exception={}",
                    exception.getClass().getSimpleName(), memberId, ip,
                    exception.getMessage());
        }

        Map<String, Object> body = Map.of("code", code, "message", message);
        objectMapper.writeValue(response.getWriter(), body);
    }


    // gateway를 거친 경우 웹 서버는 클라이언트의 실제 IP 주소가 아닌 GateWay IP를 주기 때문에
    // 진짜 클라이언트 IP를 가져오기 위한 메서드
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}