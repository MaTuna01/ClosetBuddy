package io.codebuddy.userservice.domain.common.config;

import io.codebuddy.userservice.domain.common.app.JwtTokenProvider;
import io.codebuddy.userservice.domain.common.model.dto.TokenBody;
import io.codebuddy.userservice.domain.common.security.auth.MemberDetails;
import io.codebuddy.userservice.domain.oauth.service.OauthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
/*
    요청으로 들어온 JWT 문자열을 해석해서(파싱해서) 어떤 사용자 요청인지 알아낸 다음,
    그 사용자 정보를 Spring Security가 이해하는 Authentication 객체로 만들어 주는 과정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final OauthService oauthService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if ( token != null && jwtTokenProvider.validate(token) ) {

            TokenBody tokenBody = jwtTokenProvider.parseJwt(token);
            MemberDetails memberPrincipalDetails = oauthService.getMemberDetailsById(tokenBody.getMemberId());

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    memberPrincipalDetails, token, memberPrincipalDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication); //SecurityContextHolder에 인증 저장

        }

        filterChain.doFilter(request, response);

    }

    private String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");

        if ( bearerToken != null && bearerToken.startsWith("Bearer ") ) {
            return bearerToken.substring(7);
        }

        return null;

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/signup");
    }


}
