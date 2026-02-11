package io.codebuddy.closetbuddy.domain.catalog.products.exception;

import io.codebuddy.closetbuddy.domain.catalog.web.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@Slf4j
public class StockExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleStockException(StockException e) {
        StockErrorCode errorCode = e.getErrorCode();

        // 에러를 잡아 서버 로그로 출력
        log.warn("Stock Error : {} - {}", errorCode, e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.name(), errorCode.getMessage(), Instant.now()));
    }
}
