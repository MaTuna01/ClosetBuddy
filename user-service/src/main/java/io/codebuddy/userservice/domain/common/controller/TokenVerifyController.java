package io.codebuddy.userservice.domain.common.controller;

import io.codebuddy.userservice.domain.common.app.JwtTokenProvider;
import io.codebuddy.userservice.domain.common.model.dto.TokenBody;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class TokenVerifyController {

    private final JwtTokenProvider jwtTokenProvider;

    public TokenVerifyController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public record VerifyResponse(Long userId, String role) {
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyResponse> verify(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = extractBearerToken(authHeader);
        if (token == null || !jwtTokenProvider.validate(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TokenBody body = jwtTokenProvider.parseJwt(token);
        return ResponseEntity.ok(new VerifyResponse(body.getMemberId(), body.getRole().name()));
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}