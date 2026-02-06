package io.codebuddy.closetbuddy.domain.orders.exception;

import java.time.Instant;

public record OrderErrorResponse(
        String code,
        String message,
        Instant timestamp
) {

}