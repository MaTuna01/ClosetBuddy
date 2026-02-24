package io.codebuddy.closetbuddy.domain.recommend.exception;

import lombok.Getter;

@Getter
public class RecommendException extends RuntimeException {

    private final RecommendErrorCode errorCode;

    public RecommendException(RecommendErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
