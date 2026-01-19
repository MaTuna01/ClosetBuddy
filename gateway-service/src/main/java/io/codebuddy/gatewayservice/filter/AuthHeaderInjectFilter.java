package io.codebuddy.gatewayservice.filter;


import io.codebuddy.gatewayservice.security.TokenVerifier;import io.codebuddy.gatewayservice.security.VerifiedUser;import io.codebuddy.gatewayservice.web.MutableHttpServletRequest;import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthHeaderInjectFilter extends OncePerRequestFilter {

    private final TokenVerifier tokenVerifier;

    public AuthHeaderInjectFilter(TokenVerifier tokenVerifier) {
        this.tokenVerifier = tokenVerifier;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null) {
            VerifiedUser verified = tokenVerifier.verify(authHeader);

            //헤더 주입
            MutableHttpServletRequest wrapped = new MutableHttpServletRequest(request);
            wrapped.putHeader("X-User-Id", verified.userId());
            wrapped.putHeader("X-User-Role", verified.role());

            filterChain.doFilter(wrapped, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
