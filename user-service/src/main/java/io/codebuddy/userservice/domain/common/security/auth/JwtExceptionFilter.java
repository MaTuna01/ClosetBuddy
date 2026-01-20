package io.codebuddy.userservice.domain.common.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import io.jsonwebtoken.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

// JwtTokenProvider 에서 난 예외를 JwtAuthenticationFilter 를 거쳐서 예외를 발생시키는 메서드
// 제시된 토큰의 상태를 알리는 메서드
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        try {
            // 다음 필터(JwtAuthenticationFilter)로 진행
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // 토큰 만료
            log.warn("토큰 만료: {}", e.getMessage());
            setErrorResponse(response, "TOKEN_EXPIRED", "토큰이 만료되었습니다. 토큰을 재발급 받으세요.");

        } catch (SignatureException e) {
            // 토큰 서명 위조/변조
            log.error("토큰 위조 감지: {}", e.getMessage());
            setErrorResponse(response, "TOKEN_INVALID_SIGNATURE", "유효하지 않은 토큰 서명입니다.");

        } catch (MalformedJwtException e) {
            // 토큰 형식 오류
            log.error("토큰 형식 오류: {}", e.getMessage());
            setErrorResponse(response, "TOKEN_MALFORMED", "토큰 형식이 올바르지 않습니다.");

        } catch (JwtException | IllegalArgumentException e) {
            // 기타 JWT 관련 오류
            log.error("JWT 처리 오류: {}", e.getMessage());
            setErrorResponse(response, "TOKEN_ERROR", "토큰 처리 중 오류가 발생했습니다.");
        }
    }

    private void setErrorResponse(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("message", message);

        objectMapper.writeValue(response.getWriter(), body);
    }
}
