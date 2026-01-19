package io.codebuddy.gatewayservice.filter;

import io.codebuddy.gatewayservice.security.JwtTokenVerifier;import io.codebuddy.gatewayservice.security.TokenVerifier;import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenVerifierConfig {
    @Bean
    public TokenVerifier tokenVerifier() {
        return new JwtTokenVerifier();
    }
}
