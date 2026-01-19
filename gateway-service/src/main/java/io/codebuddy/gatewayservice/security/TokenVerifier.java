package io.codebuddy.gatewayservice.security;

public interface TokenVerifier {
    VerifiedUser verify(String authHeader);
}
