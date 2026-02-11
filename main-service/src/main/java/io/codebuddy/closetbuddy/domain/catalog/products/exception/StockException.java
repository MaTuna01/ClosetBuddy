package io.codebuddy.closetbuddy.domain.catalog.products.exception;

import lombok.Getter;

@Getter
public class StockException extends RuntimeException {
    private final StockErrorCode errorCode;
    public StockException(StockErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}