package io.codebuddy.closetbuddy.domain.common.web.dto;

public record RecommendResult<T>(
        String message,
        T data
) {
    public static <T>  RecommendResult<T> success (String message, T data) { return new RecommendResult<>(message, data); }

    public static <T> RecommendResult<T> success (String message) { return new RecommendResult<>(message, null); }
}
