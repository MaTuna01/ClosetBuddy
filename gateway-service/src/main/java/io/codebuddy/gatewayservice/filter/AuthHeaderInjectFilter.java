package io.codebuddy.gatewayservice.filter;

import io.codebuddy.gatewayservice.security.TokenVerifier;
import io.codebuddy.gatewayservice.security.VerifiedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
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
            } catch (HttpClientErrorException e) {
                // User-Service에서 받은 상태 코드를 그대로 설정
                response.setStatus(e.getStatusCode().value());

                // 응답 타입 설정 (JSON)
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                // User-Service가 보낸 에러 바디(JSON)를 그대로 쓰기
                response.getWriter().write(e.getResponseBodyAsString());

                return; // 필터 체인 중단
            }
            catch (Exception ex) {
                // 그 외 예상치 못한 에러 처리
                ex.printStackTrace();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

        }
        filterChain.doFilter(request, response);
    }
}
