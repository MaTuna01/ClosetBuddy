package io.codebuddy.gatewayservice.security;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenVerifier implements TokenVerifier {
    @Override
    public VerifiedUser verify(String authHeader) {
        //jwt 검증 로직
        return new VerifiedUser("userId", "ROLE_MEMBER");
    }
}
