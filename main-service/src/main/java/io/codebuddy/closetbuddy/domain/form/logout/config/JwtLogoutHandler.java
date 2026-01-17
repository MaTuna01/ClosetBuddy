package io.codebuddy.closetbuddy.domain.form.logout.config;

import io.codebuddy.closetbuddy.domain.common.repository.TokenRepository;
import io.codebuddy.closetbuddy.domain.common.security.auth.MemberDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

// 로그아웃 핸들러
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final TokenRepository tokenRepository;
    // 로그아웃할 때 특정 사용자의 유효한 Refresh token을 찾아서 있으면 refresh token을 없애고 blacklist에 넣는다.
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication){

        //refresh token을 "헤더"로 받는 경우
        if (authentication == null || !(authentication.getPrincipal() instanceof MemberDetails memberPrincipalDetails)) {
            return;
        }

        Long memberId = memberPrincipalDetails.getMember().getId(); //현재 로그인한 사용자(주체)의 DB id를 꺼냄.
        tokenRepository.findValidRefToken(memberId).ifPresent(rt -> {tokenRepository.addBlackList(rt);
        tokenRepository.delete(rt);
        // 이 사용자에게 아직 유효한 refresh token이 저장돼 있으면 refresh token을 블랙리스트에 넣고 저장소에서는 삭제
        });
    }
}
