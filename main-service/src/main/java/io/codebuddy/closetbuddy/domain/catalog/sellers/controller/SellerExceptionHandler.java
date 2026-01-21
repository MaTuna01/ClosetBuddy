package io.codebuddy.closetbuddy.domain.catalog.sellers.controller;

import io.codebuddy.closetbuddy.domain.catalog.sellers.exception.SellerErrorCode;
import io.codebuddy.closetbuddy.domain.catalog.sellers.exception.SellerException;
import io.codebuddy.closetbuddy.domain.catalog.web.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

// SellerApiController의 예외만 잡도록 격리
@RestControllerAdvice(assignableTypes = SellerApiController.class)
public class SellerExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SellerExceptionHandler.class);

    //Seller 서비스 로직에서 발생하는 예외에 대한 핸들러
    @ExceptionHandler(SellerException.class)
    public ResponseEntity<ErrorResponse> handleSellerException(SellerException e) {
        SellerErrorCode errorCode = e.getErrorCode();
        //예외를 잡아 서버 로그로 출력
        log.warn("Seller Domain Error: {} - {}", errorCode, e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse("ALREADY_REGISTERED", errorCode.getMessage(), Instant.now()));
    }

    //그 외 서버에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        String errorMessage = "판매자 서비스에 문제가 발생했습니다. 관리자에게 문의하세요.";
        log.error("Seller Service Unhandled Error: ", e); // 진짜 서버 에러는 Error 로그
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(null, errorMessage, Instant.now()));
    }
}