package io.codebuddy.closetbuddy.domain.catalog.stores.exception;

import lombok.Getter;

@Getter
public class StoreException extends RuntimeException {
    private final StoreErrorCode errorCode;

    public StoreException(StoreErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
