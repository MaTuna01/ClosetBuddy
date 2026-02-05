package org.dev.orderservice.domain.carts.exception;


import org.dev.orderservice.domain.carts.controller.CartController;
import org.dev.orderservice.domain.carts.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice(assignableTypes =  {CartController.class, CartService.class})

public class CartExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CartExceptionHandler.class);

    // Cart 서비스 로직에서 발생하는 예외 핸들러
    @ExceptionHandler(CartException.class)
    public ResponseEntity<CartErrorResponse> handleCartException(CartException e) {
        CartErrorCode errorCode = e.getErrorCode();
        //에외를 잡아 서버 로그로 출력
        log.warn("Cart Domain Error: {} - {}", errorCode, e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new CartErrorResponse(errorCode.name(), errorCode.getMessage(), Instant.now()));
    }
    // 요청 값 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CartErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = "요청 값이 유효하지 않습니다.";
        log.warn("Seller Validation Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CartErrorResponse("INVALID_REQUEST", errorMessage, Instant.now()));
    }

    //그 외 서버에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CartErrorResponse> handleException(Exception e) {
        String errorMessage = "장바구니 서비스에 문제가 발생했습니다. 관리자에게 문의하세요.";
        log.error("Cart Service Unhandled Error: ",e);
        return ResponseEntity.internalServerError()
                .body(new CartErrorResponse(null, errorMessage, Instant.now()));
    }

}
