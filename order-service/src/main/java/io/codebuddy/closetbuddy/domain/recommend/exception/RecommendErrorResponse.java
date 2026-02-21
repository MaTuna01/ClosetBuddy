package io.codebuddy.closetbuddy.domain.recommend.exception;

import java.time.Instant;

public record RecommendErrorResponse(
        String code,
        String message,
        Instant timestamp
) {

}