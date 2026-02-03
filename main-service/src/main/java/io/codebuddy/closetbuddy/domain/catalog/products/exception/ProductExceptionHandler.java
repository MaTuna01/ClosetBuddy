package io.codebuddy.closetbuddy.domain.catalog.products.exception;

import io.codebuddy.closetbuddy.domain.catalog.products.controller.ProductApiController;
import io.codebuddy.closetbuddy.domain.catalog.products.service.ProductService;
import io.codebuddy.closetbuddy.domain.catalog.web.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

//product 도메인에 걸쳐 예외를 처리하는 핸들러
@RestControllerAdvice(assignableTypes =  {ProductApiController.class, ProductService.class})

public class ProductExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ProductExceptionHandler.class);

    //Product 서비스 로직에서 발생하는 예외 핸들러
    @ExceptionHandler(ProductException.class)
    public ResponseEntity<ErrorResponse> handleProductException(ProductException e) {
        ProductErrorCode errorCode = e.getErrorCode();
        //에외를 잡아 서버 로그로 출력
        log.warn("Product Domain Error: {} - {}", errorCode, e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.name(), errorCode.getMessage(), Instant.now()));
    }
    //요청 값 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = "요청 값이 유효하지 않습니다.";
        log.warn("Seller Validation Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_REQUEST", errorMessage, Instant.now()));
    }



}
