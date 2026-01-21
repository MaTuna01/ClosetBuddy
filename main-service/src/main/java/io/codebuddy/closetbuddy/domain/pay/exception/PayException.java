package io.codebuddy.closetbuddy.domain.pay.exception;

import lombok.Getter;

@Getter
public class PayException extends RuntimeException{

    private final ErrorCode errorCode;

    public PayException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public PayException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
    }
}
