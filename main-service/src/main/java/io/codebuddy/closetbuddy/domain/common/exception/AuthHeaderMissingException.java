package io.codebuddy.closetbuddy.domain.common.exception;

public class AuthHeaderMissingException extends RuntimeException {
    public AuthHeaderMissingException(String message) {
        super(message);
    }
}