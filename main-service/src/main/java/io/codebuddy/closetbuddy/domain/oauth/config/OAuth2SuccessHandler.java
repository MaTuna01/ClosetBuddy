package io.codebuddy.closetbuddy.domain.oauth.config;

import io.codebuddy.closetbuddy.domain.common.security.auth.MemberDetails;
import io.codebuddy.closetbuddy.domain.common.model.dto.TokenPair;
import io.codebuddy.closetbuddy.domain.common.app.JwtTokenProvider;
import io.codebuddy.closetbuddy.domain.oauth.service.OauthService;
import io.codebuddy.closetbuddy.domain.common.model.entity.RefreshToken;
import io.codebuddy.closetbuddy.domain.common.model.entity.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

//OAuth2 로그인 성공 시 JWT 토큰 발급
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${custom.jwt.redirection.base}")
    private String baseUrl;

    private final OauthService oauthService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        MemberDetails principal = (MemberDetails) authentication.getPrincipal();
        Member findMember = oauthService.getById(principal.getId());

        HashMap<String, String> params = new HashMap<>();
        Optional<RefreshToken> refreshTokenOptional = jwtTokenProvider.findRefreshToken(principal.getId());
        if ( refreshTokenOptional.isEmpty() ) {
            // 신규 로그인: Access + Refresh 토큰 쌍 발급
            TokenPair tokenPair = jwtTokenProvider.generateTokenPair(findMember);
            params.put("access", tokenPair.getAccessToken());
            params.put("refresh", tokenPair.getRefreshToken());
        } else {
            // 기존 Refresh 토큰 있음: Access 토큰만 재발급
            String accessToken = jwtTokenProvider.issueAccessToken(principal.getId(), principal.getRole());
            params.put("access", accessToken);
            params.put("refresh", refreshTokenOptional.get().getRefreshToken());
        }

        // 리다이렉트 URL 생성
        String urlStr = genUrlStr(params);
        getRedirectStrategy().sendRedirect(request, response, urlStr);

    }

    // OAuth2 로그인 성공 후 JWT 토큰을 포함한 리다이렉트 URL을 안전하게 생성하는 메서드
    private String genUrlStr(HashMap<String, String> params) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("access", params.get("access"))
                .queryParam("refresh", params.get("refresh"))
                .build()
                .toUri()
                .toString();
    }

}
