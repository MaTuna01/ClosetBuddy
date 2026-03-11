package io.codebuddy.closetbuddy.domain.catalog.stores.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum StoreErrorCode {
    // 권한 없음 등 (403 Forbidden)
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "판매자 권한이 없습니다."),
    //
    NOT_OWNER(HttpStatus.FORBIDDEN, "당신의 상점을 찾을 수 없습니다."),
    // 상점을 찾을 수 없음
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "상점 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    StoreErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
