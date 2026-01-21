package io.codebuddy.closetbuddy.domain.common.web;

import io.codebuddy.closetbuddy.domain.common.exception.AuthHeaderMissingException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthHeaderMissingException.class)
    public ResponseEntity<ErrorResponse> handleAuthHeaderMissing(AuthHeaderMissingException ex) {
        ErrorResponse response = new ErrorResponse(
                "AUTH_HEADER_MISSING",
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}