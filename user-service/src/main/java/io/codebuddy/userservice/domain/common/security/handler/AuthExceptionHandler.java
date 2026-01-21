package io.codebuddy.userservice.domain.common.security.handler;

import io.codebuddy.userservice.domain.common.exception.InvalidTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/*
@ExceptionHandler: @controller, @RestController가 적용된 Bean 내에서 발생하는 예외를 잡아서 하나의 메서드에서 처리해주는 기능을 한다.
@RestControllerAdvice: 모든 @Controller, 즉 전역에서 발생할 수 있는 예외를 잡아주는 어노테이션
TokenRefreshService 관련 예외처리 메서드
해당 어노테이션들을 써준 이유: TokenRefreshService에서 예외가 터지면 컨트롤러로 던져지기 때문에
 */
@RestControllerAdvice
public class AuthExceptionHandler {
    // Refresh Token 검증 실패 처리
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidTokenException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", "INVALID_REFRESH_TOKEN");
        body.put("message", e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // 기타 예상치 못한 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception e) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", "INTERNAL_SERVER_ERROR");
        body.put("message", "서버 오류가 발생했습니다.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
