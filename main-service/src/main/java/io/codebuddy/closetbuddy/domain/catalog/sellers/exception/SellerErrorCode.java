package io.codebuddy.closetbuddy.domain.catalog.sellers.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SellerErrorCode {
    // 이미 등록된 판매자 (409 Conflict)
    ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 등록된 판매자입니다."),

    // 이미 사용중인 판매자 이름 (409 Conflict)
    SELLER_NAME_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 판매자 이름입니다."),

    // 판매자를 찾을 수 없음 (404 Not Found)
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "판매자 정보를 찾을 수 없습니다."),

    // 권한 없음 등 (403 Forbidden)
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "판매자 권한이 없습니다."),

    // 판매자 역할 부여 실패 (503 Service Unavailable)
    ROLE_GRANT_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "판매자 역할 부여에 실패했습니다. 잠시 후 다시 시도해주세요."),

    //판매자 역할 해제 실패(503 Service Unavailable)
    ROLE_REVOKE_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "판매자 역할 해제에 실패했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String message;

    SellerErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
