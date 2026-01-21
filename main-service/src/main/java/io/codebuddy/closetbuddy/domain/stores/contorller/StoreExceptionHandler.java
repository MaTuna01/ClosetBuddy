package io.codebuddy.closetbuddy.domain.stores.contorller;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackageClasses = StoreApiController.class)
@Slf4j
public class StoreExceptionHandler {

    //상점 요청 검증 메시지 핸들러 응답 출력
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        //서버 로그 출력
        log.warn("상점 요청 검증 실패: {}", message);
        return errorResponse(HttpStatus.BAD_REQUEST, message);
    }

    //상점 요청 파라미터 제약조건 위반 예외 핸들러 응답 출력
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        //서버 로그 출력
        log.warn("상점 요청 제약 조건 위반: {}", ex.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        HttpStatus status = resolveStatus(ex.getMessage());
        log.warn("상점 처리 오류: {}", ex.getMessage());
        return errorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        log.error("상점 처리 중 서버 오류", ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "상점 처리 중 오류가 발생했습니다.");
    }

    private HttpStatus resolveStatus(String message) {
        if (message != null && (message.contains("없습니다") || message.contains("존재하지"))) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private ResponseEntity<Map<String, String>> errorResponse(HttpStatus status, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        return ResponseEntity.status(status).body(error);
    }
}
