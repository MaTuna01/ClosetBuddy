package io.codebuddy.gatewayservice.filter;

import io.codebuddy.gatewayservice.security.TokenVerifier;
import io.codebuddy.gatewayservice.security.VerifiedUser;
import io.codebuddy.gatewayservice.web.MutableHttpServletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//헤더에 X-User-Id, X-User-Role 주입
@Component
public class AuthHeaderInjectFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-USER-ID";
    private static final String USER_ROLE_HEADER = "X-USER-ROLE";

    private final TokenVerifier tokenVerifier;

    public AuthHeaderInjectFilter(TokenVerifier tokenVerifier) {
        this.tokenVerifier = tokenVerifier;
    }

    // Oauth 경로는 인증 전 단계이므로 JWT 검증 필터를 건너뛰도록 설정
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        HttpServletRequest requestToUse = request;

        if (authHeader != null) {
            VerifiedUser verified;
            try {
                verified = tokenVerifier.verify(authHeader);

                // 헤더 주입: MutableHttpServletRequest를 사용하여 X-USER-ID, X-USER-ROLE 헤더 추가
                MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
                mutableRequest.putHeader(USER_ID_HEADER, verified.userId());
                mutableRequest.putHeader(USER_ROLE_HEADER, verified.role());

                // 추가할 코드: user-service가 아닌 서비스로 라우팅되는 경우 Authorization 헤더 제거
                String requestPath = request.getRequestURI();
                if (!isUserServicePath(requestPath)) {
                    mutableRequest.removeHeader("Authorization");
                }

                requestToUse = mutableRequest;

            } catch (HttpClientErrorException e) {
                // User-Service에서 받은 상태 코드를 그대로 설정
                response.setStatus(e.getStatusCode().value());

                // 응답 타입 설정 (JSON)
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                // User-Service가 보낸 에러 바디(JSON)를 그대로 쓰기
                response.getWriter().write(e.getResponseBodyAsString());

                return; // 필터 체인 중단
            } catch (Exception ex) {
                // 그 외 예상치 못한 에러 처리
                ex.printStackTrace();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(requestToUse, response);
    }

    /**
     * user-service로 라우팅되는 경로인지 확인
     * user-service는 JwtAuthenticationFilter를 통해 JWT 토큰 기반 인증을 사용하므로
     * Authorization 헤더를 유지해야 하므로 user-service로 라우팅 할 시 헤더 유지
     *
     * user-service의 MemberController는 @AuthenticationPrincipal을 사용하여
     * SecurityContextHolder에서 인증 정보를 가져오므로 JWT가 필수
     *
     * 요청 경로를 파악하고 필요에따라 헤더 유지
     */
    private boolean isUserServicePath(String path) {
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/api/v1/authc")
                || path.startsWith("/api/v1/members/");
    }

}
