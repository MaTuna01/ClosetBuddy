package io.codebuddy.closetbuddy.domain.catalog.web;

public class AuthHeaderMissingException extends RuntimeException {
    public AuthHeaderMissingException(String message) {
        super(message);
    }
}