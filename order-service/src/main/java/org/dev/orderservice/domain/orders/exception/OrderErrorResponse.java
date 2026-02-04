package org.dev.orderservice.domain.orders.exception;

import java.time.Instant;

public record OrderErrorResponse(
        String code,
        String message,
        Instant timestamp
) {

}