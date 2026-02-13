package io.codebuddy.payservice.domain.common.exception;

public class AuthHeaderMissingException extends RuntimeException {
    public AuthHeaderMissingException(String message) {
        super(message);
    }
}
