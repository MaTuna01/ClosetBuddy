package io.codebuddy.closetbuddy.domain.carts.exception;

import java.time.Instant;

public record CartErrorResponse(
        String code,
        String message,
        Instant timestamp
) {

}