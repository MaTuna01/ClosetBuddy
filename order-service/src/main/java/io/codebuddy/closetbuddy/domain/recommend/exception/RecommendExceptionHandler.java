package io.codebuddy.closetbuddy.domain.recommend.exception;

import io.codebuddy.closetbuddy.domain.recommend.controller.RecommendController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice(assignableTypes = { RecommendController.class })
public class RecommendExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RecommendExceptionHandler.class);

    // Recommend 서비스 로직에서 발생하는 예외 핸들러
    @ExceptionHandler(RecommendException.class)
    public ResponseEntity<RecommendErrorResponse> handleRecommendException(RecommendException e) {
        RecommendErrorCode errorCode = e.getErrorCode();
        // 예외를 잡아 서버 로그로 출력
        log.warn("Recommend Domain Error: {} - {}", errorCode, e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new RecommendErrorResponse(errorCode.name(), errorCode.getMessage(), Instant.now()));
    }

    // 요청 값 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RecommendErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = "요청 값이 유효하지 않습니다.";
        log.warn("Recommend Validation Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RecommendErrorResponse("INVALID_REQUEST", errorMessage, Instant.now()));
    }

    // 그 외 서버 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RecommendErrorResponse> handleException(Exception e) {
        String errorMessage = "추천 서비스에 문제가 발생했습니다. 관리자에게 문의하세요.";
        log.error("Recommend Service Unhandled Error: ", e);
        return ResponseEntity.internalServerError()
                .body(new RecommendErrorResponse(null, errorMessage, Instant.now()));
    }

}
