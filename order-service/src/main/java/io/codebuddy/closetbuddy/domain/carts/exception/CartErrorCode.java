package io.codebuddy.closetbuddy.domain.carts.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CartErrorCode {

    // 사용자의 장바구니가 아닐 경우(403 Forbidden)
    NOT_OWNER(HttpStatus.FORBIDDEN, "사용자의 장바구니가 아닙니다."),

    // 장바구니를 찾을 수 없을 경우(404 Not Found)
    NOT_MEMBER(HttpStatus.NOT_FOUND, "회원이 존재하지 않습니다."),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 안에 상품이 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품을 찾을 수 없습니다."),

    // 사용자의 장바구니가 이미 만들어져있는 상태일 때
    CART_ALREADY_EXITS(HttpStatus.CONFLICT, "장바구니가 이미 존재합니다.");

    private final HttpStatus status;
    private final String message;

    CartErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
