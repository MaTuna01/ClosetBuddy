package io.codebuddy.userservice.domain.common.service;

import io.codebuddy.userservice.domain.common.app.JwtTokenProvider;
import io.codebuddy.userservice.domain.common.exception.InvalidTokenException;
import io.codebuddy.userservice.domain.common.model.dto.TokenBody;
import io.codebuddy.userservice.domain.common.model.entity.RefreshToken;
import io.codebuddy.userservice.domain.common.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
클라이언트가 준 refresh 토큰으로 access 토큰만 재발급 하는 서비스 로직
 */
@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;

    @Transactional
    public String refreshAccessToken(String refreshToken) {


        RefreshToken saved = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("REFRESH_NOT_FOUND"));

        //1. blacklist 확인
        if (tokenRepository.isBlacklisted(saved)) {
            throw new InvalidTokenException("BLACKLISTED_REFRESH_TOKEN");
        }

        //2. refresh JWT 유효성 검증
        jwtTokenProvider.validate(refreshToken);

        // 3. refresh에서 memberid 추출
        TokenBody body = jwtTokenProvider.parseJwt(refreshToken);
        Long memberId = body.getMemberId();


        // 4) DB에 저장된 refresh가 존재 + 블랙리스트 아님 + 문자열 일치
        RefreshToken current = tokenRepository.findValidRefToken(memberId)
                .orElseThrow(() -> new InvalidTokenException("REFRESH_NOT_FOUND_OR_BLACKLISTED"));

        if (!current.getRefreshToken().equals(refreshToken)) {
            throw new InvalidTokenException("REFRESH_MISMATCH");
        }

        // 5) access 발급 (role은 member에서 가져오는 식으로)
        return jwtTokenProvider.issueAccessToken(memberId, saved.getMember().getRole());
    }

}
