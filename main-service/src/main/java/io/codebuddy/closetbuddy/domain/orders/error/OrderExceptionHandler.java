//package io.codebuddy.closetbuddy.domain.orders.error;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//@Slf4j
//@RestControllerAdvice
//public class OrderExceptionHandler {
//
//    // 직접 만든 예외 처리
//    @ExceptionHandler(CustomException.class)
//    public ResponseEntity<OrderErrorResponse> handleCustomException(CustomException e) {
//        log.error("handleCustomException: {}", e.getErrorCode());
////        return OrderErrorResponse.toResponseEntity(e.getErrorCode());
////    }
//
//
////    // DTO Valid 검증 처리
////    public ResponseEntity<OrderErrorCode> handleValidException(MethodArgumentNotValidException e) {
////
////    }
////}
