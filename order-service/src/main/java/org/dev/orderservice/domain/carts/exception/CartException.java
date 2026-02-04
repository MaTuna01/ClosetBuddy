package org.dev.orderservice.domain.carts.exception;

import lombok.Getter;

@Getter
public class CartException extends RuntimeException {

    private final CartErrorCode errorCode;

    public CartException(CartErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
