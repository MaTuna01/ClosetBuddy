package io.codebuddy.closetbuddy.domain.catalog.sellers.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SellerErrorCode {
    // 이미 등록된 판매자 (409 Conflict)
    ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 등록된 판매자입니다."),

    // 판매자를 찾을 수 없음 (404 Not Found)
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "판매자 정보를 찾을 수 없습니다."),

    // 권한 없음 등 (403 Forbidden)
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "판매자 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;

    SellerErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
