package io.codebuddy.closetbuddy.domain.pay.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossErrorResponse {
    private String code;
    private String message;
}