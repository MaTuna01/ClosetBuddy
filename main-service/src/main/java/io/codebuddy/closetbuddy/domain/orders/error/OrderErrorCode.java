package io.codebuddy.closetbuddy.domain.orders.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OrderErrorCode {

    // 400 BAD_REQUEST
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "요청 타입이 일치하지 않습니다."),

    // 404 NOT_FOUND
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주문 내역을 찾을 수 없습니다."),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 내역이 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품 내역이 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."),

    // 409 DUPLICATED
    DUPLICATED_DATA(HttpStatus.CONFLICT, "중복된 데이터 값입니다."),

    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 에러가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
