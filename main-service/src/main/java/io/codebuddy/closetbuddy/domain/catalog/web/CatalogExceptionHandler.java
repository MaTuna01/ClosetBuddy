package io.codebuddy.closetbuddy.domain.catalog.web;

import io.codebuddy.closetbuddy.domain.catalog.products.service.ProductService;
import io.codebuddy.closetbuddy.domain.catalog.sellers.service.SellerService;
import io.codebuddy.closetbuddy.domain.catalog.stores.service.StoreService;
import io.codebuddy.closetbuddy.domain.common.exception.AuthHeaderMissingException;
import io.codebuddy.closetbuddy.domain.catalog.products.controller.ProductController;
import io.codebuddy.closetbuddy.domain.catalog.sellers.controller.SellerApiController;
import io.codebuddy.closetbuddy.domain.catalog.stores.controller.StoreApiController;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice(assignableTypes = {
        ProductController.class,
        StoreApiController.class,
        SellerApiController.class,
        ProductService.class,
        StoreService.class,
        SellerService.class
})
public class CatalogExceptionHandler {

    // 공통 인증 헤더 누락 예외 (Catalog 도메인 전체 적용)
    @ExceptionHandler(AuthHeaderMissingException.class)
    public ResponseEntity<ErrorResponse> handleAuthHeaderMissing(AuthHeaderMissingException ex) {
        log.warn("[Catalog] Auth Header Missing: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("AUTH_HEADER_MISSING", ex.getMessage(), Instant.now()));
    }

    // 유효성 검사 (@Valid) 통합 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();

        // 여러 필드 에러가 있을 경우, 첫 번째 메시지만 보내거나 합쳐서 반환
        String firstErrorMessage = bindingResult.getFieldErrors().stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("잘못된 요청입니다.");
        //서버 로그 출력
        log.warn("Seller Validation Fail: {}", firstErrorMessage);

        return ResponseEntity.badRequest()
                .body(new ErrorResponse("400 BAD_REQUEST", firstErrorMessage,Instant.now()));
    }
}