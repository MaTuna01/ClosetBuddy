package io.codebuddy.closetbuddy.domain.products.controller;

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

//product 도메인에 걸쳐 예외를 처리하는 핸들러
@RestControllerAdvice(basePackageClasses = ProductApiController.class)
@Slf4j
public class ProductExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        //상품 요청에 대한 예외 메시지 추출
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        //서버 로그에 검증 메시지 출력
        log.warn("상품 요청 검증 실패: {}", message);
        //요청 반환으로 메시지 출력
        return errorResponse(HttpStatus.BAD_REQUEST, message);
    }

    //상품 요청 제약 조건 위반 예외 처리 핸ㄷ늘러
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException exception) {
        //원인 서버 로그 출력
        log.warn("상품 요청 제약 조건 위반: {}", exception.getMessage());
        //반환값 메시지 출력
        return errorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    //요청값 유효성 예외처리 핸들러
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        HttpStatus status = resolveStatus(ex.getMessage());
        //서버 로그 출력
        log.warn("상품 처리 오류: {}", ex.getMessage());
        //예외 응답 메시지 반환
        return errorResponse(status, ex.getMessage());
    }

    //서버에러(주로) 핸들러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        log.error("상품 처리 중 서버 오류", ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "상품 처리 중 오류가 발생했습니다.");
    }

    //상품 삭제 혹은 요청 유저 존재 확인 예외처리 핸들러
    private HttpStatus resolveStatus(String message) {
        if (message != null && (message.contains("없습니다") || message.contains("존재하지"))) {
            log.warn(message);
            return HttpStatus.NOT_FOUND;
        }
        log.warn(message);
        return HttpStatus.BAD_REQUEST;
    }

    private ResponseEntity<Map<String, String>> errorResponse(HttpStatus status, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        return ResponseEntity.status(status).body(error);
    }
}
