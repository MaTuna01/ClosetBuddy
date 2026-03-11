package io.codebuddy.closetbuddy.domain.catalog.web;

import java.time.Instant;

public record ErrorResponse(
        String code,
        String message,
        Instant timestamp) { }