package io.codebuddy.closetbuddy.domain.catalog.stores.exception;

import io.codebuddy.closetbuddy.domain.catalog.sellers.exception.SellerException;
import io.codebuddy.closetbuddy.domain.catalog.stores.controller.StoreApiController;
import io.codebuddy.closetbuddy.domain.catalog.stores.service.StoreService;
import io.codebuddy.closetbuddy.domain.catalog.web.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

//Store 도메인의 예외만 잡도록 격리
@RestControllerAdvice(assignableTypes = {StoreApiController.class, StoreService.class})
public class StoreExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(StoreExceptionHandler.class);

    @ExceptionHandler(SellerException.class)
    public ResponseEntity<ErrorResponse> handleStoreException(StoreException e) {
        StoreErrorCode errorCode = e.getErrorCode();
        //예외를 잡아 서버 로그로 출력
        log.warn("Store Domain Error: {} - {}", errorCode, e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.name(), errorCode.getMessage(), Instant.now()));
    }

    //그 외 서버에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        String errorMessage = "상점 서비스에 문제가 발생했습니다. 관리자에게 문의하세요.";
        log.error("Store Service Unhandled Error: ", e); // 진짜 서버 에러는 Error 로그
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(null, errorMessage, Instant.now()));
    }

}
