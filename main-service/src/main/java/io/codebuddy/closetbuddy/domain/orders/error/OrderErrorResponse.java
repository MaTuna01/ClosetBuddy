package io.codebuddy.closetbuddy.domain.orders.error;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

@Slf4j
@Getter
@Builder
public class OrderErrorResponse {
    private final int status;
    private final String error;
    private final String code;
    private final String message;


    // 기본 메서드
    public ResponseEntity<OrderErrorResponse> toResponseEntity(OrderErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(OrderErrorResponse.builder()
                        .status(errorCode.getStatus().value())
                        .error(errorCode.getStatus().name())
                        .code(errorCode.name())
                        .message(errorCode.getMessage())
                        .build()
                );
    }

    // Valid 메서드
    public static ResponseEntity<OrderErrorResponse> toResponseEntity(OrderErrorCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(OrderErrorResponse.builder()
                        .status(errorCode.getStatus().value())
                        .error(errorCode.getStatus().name())
                        .code(errorCode.name())
                        .message(errorCode.getMessage())
                        .build()
                );
    }

}
