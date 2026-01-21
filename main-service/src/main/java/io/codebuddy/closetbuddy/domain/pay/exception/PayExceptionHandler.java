package io.codebuddy.closetbuddy.domain.pay.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "io.codebuddy.closetbuddy.domain.pay")
public class PayExceptionHandler {

    // @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        // 에러가 발생한 필드와 메시지를 추출
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("messages", errors); // 상세 에러 내용

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(PayException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(PayException ex) {
        Map<String, Object> response = new HashMap<>();
        ErrorCode errorCode = ex.getErrorCode();

        response.put("status", errorCode.getStatus().value());
        response.put("error", errorCode.name());
        response.put("message", errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }



}
