package io.codebuddy.closetbuddy.domain.catalog.stores.exception;

public class StoreException extends RuntimeException {
    private final StoreErrorCode errorCode;

    public StoreException(StoreErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public StoreErrorCode getErrorCode() {
        return errorCode;
    }
}
