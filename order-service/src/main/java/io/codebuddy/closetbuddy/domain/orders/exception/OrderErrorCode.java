package io.codebuddy.closetbuddy.domain.orders.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderErrorCode {
    // 주문이 사용자 주문이 아닐 경우 (403 Forbidden)
    NOT_OWNER(HttpStatus.FORBIDDEN, "사용자의 주문이 아닙니다."),

    // 주문 내역을 찾을 수 없을 경우(404 Not Found)
    NOT_MEMBER(HttpStatus.NOT_FOUND, "회원이 존재하지 않습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주문 내역을 찾을 수 없습니다."),
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주문 상품을 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),

    // 상품의 재고가 적어 주문할 수 없는 경우 (409 CONFLICT)
    OUT_OF_STOCK(HttpStatus.CONFLICT, "남은 재고가 없습니다."),

    CANCEL_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY, "현재 주문 상태에서는 취소할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    OrderErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
