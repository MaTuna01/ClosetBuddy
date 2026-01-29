package io.codebuddy.closetbuddy.domain.pay.exception;

import lombok.Getter;

@Getter
public class PayException extends RuntimeException{

    private final ErrorCode errorCode;
    private String providerErrorCode; // 외부 PG사 원본 에러코드 출력

    public PayException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public PayException(ErrorCode errorCode, String providerErrorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
        this.providerErrorCode = providerErrorCode;
    }
}
