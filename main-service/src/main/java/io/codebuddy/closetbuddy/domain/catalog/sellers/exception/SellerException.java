package io.codebuddy.closetbuddy.domain.catalog.sellers.exception;

public class SellerException extends RuntimeException {
    private final SellerErrorCode errorCode;

    public SellerException(SellerErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public SellerErrorCode getErrorCode() {
        return errorCode;
    }
}
