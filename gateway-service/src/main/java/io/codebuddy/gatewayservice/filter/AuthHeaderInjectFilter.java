package io.codebuddy.gatewayservice.filter;

import io.codebuddy.gatewayservice.security.TokenVerifier;
import io.codebuddy.gatewayservice.security.VerifiedUser;
import io.codebuddy.gatewayservice.web.MutableHttpServletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


//헤더에 X-User_id, X-User-Role 주입
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
            VerifiedUser verified;
            try {
                verified = tokenVerifier.verify(authHeader);
            } catch (Exception ex) {
                ex.printStackTrace();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            //헤더 주입 -> yml 파일로 빼서 주입, 관례상 전체 대문자(추가적인 X헤더 에 많은 주입 재고)
            MutableHttpServletRequest wrapped = new MutableHttpServletRequest(request);
            wrapped.putHeader("X-USER-ID", verified.userId());
            wrapped.putHeader("X-USER-ROLE", verified.role());

            filterChain.doFilter(wrapped, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
