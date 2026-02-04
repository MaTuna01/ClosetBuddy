package org.dev.orderservice.domain.common.web;

import java.time.Instant;

public record ErrorResponse(
        String code,
        String message,
        Instant timestamp) { }