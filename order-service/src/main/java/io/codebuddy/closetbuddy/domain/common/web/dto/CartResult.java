package io.codebuddy.closetbuddy.domain.common.web.dto;

public record CartResult<T>(
        String message,
        T data
) {
    public static <T> CartResult<T> success(String message, T data) {
        return new CartResult<>(message, data);
    }

    public static <T> CartResult<T> success(String message) {
        return new CartResult<>(message, null);
    }
}
