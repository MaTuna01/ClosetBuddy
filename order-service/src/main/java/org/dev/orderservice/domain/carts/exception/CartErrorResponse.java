package org.dev.orderservice.domain.carts.exception;

import java.time.Instant;

public record CartErrorResponse(
        String code,
        String message,
        Instant timestamp
) {

}