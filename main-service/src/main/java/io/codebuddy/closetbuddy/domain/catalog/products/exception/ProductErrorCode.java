package io.codebuddy.closetbuddy.domain.catalog.products.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ProductErrorCode {
    // 상품의 주인이 아닐경우(403 Forbidden)
    NOT_OWNER(HttpStatus.FORBIDDEN, "상품의 주인이 아닙니다."),
    // 상품을 찾을 수 없을경우(404 Not Found)
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ProductErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
