package io.codebuddy.closetbuddy.domain.orders.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException{

    private final OrderErrorCode errorCode;

}
