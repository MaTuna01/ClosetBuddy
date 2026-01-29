package io.codebuddy.closetbuddy.domain.pay.exception;

import lombok.Getter;

@Getter
public class PayException extends RuntimeException{

    private final PayErrorCode payErrorCode;
    private String providerErrorCode; // 외부 PG사 원본 에러코드 출력

    public PayException(PayErrorCode payErrorCode) {
        super(payErrorCode.getMessage());
        this.payErrorCode = payErrorCode;
    }

    public PayException(PayErrorCode payErrorCode, String providerErrorCode, String detailMessage) {
        super(detailMessage);
        this.payErrorCode = payErrorCode;
        this.providerErrorCode = providerErrorCode;
    }
}
