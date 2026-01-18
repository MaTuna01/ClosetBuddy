package io.codebuddy.userservice.domain.common.app;

import io.codebuddy.userservice.domain.common.config.JwtConfiguration;
import io.codebuddy.userservice.domain.common.model.dto.Role;
import io.codebuddy.userservice.domain.common.model.dto.TokenBody;
import io.codebuddy.userservice.domain.common.model.dto.TokenPair;
import io.codebuddy.userservice.domain.common.model.entity.Member;
import io.codebuddy.userservice.domain.common.model.entity.RefreshToken;
import io.codebuddy.userservice.domain.common.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

/*
토큰 생성/검증/파싱
tokenRepository: refresh Token의 발급, 조회, 블랙 리스트 등록을 담당
jwtConfiguration: access Token과 Refresh 토큰의 재발급 정보를 담당
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtConfiguration jwtConfiguration;
    private final TokenRepository tokenRepository;

    //토큰을 두개를 묶는 메서드
    public TokenPair generateTokenPair(Member member) {

        String accessToken = issueAccessToken(member.getId(), member.getRole());
        String refreshToken = issueRefreshToken(member.getId(), member.getRole());

        tokenRepository.save(member, refreshToken);

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 특정 사용자의 유효한 RefreshToken이 DB에 있는지 확인
    public Optional<RefreshToken> findRefreshToken(Long memberId) {
        return tokenRepository.findValidRefToken(memberId);
    }

    // Access 토큰 생성
    public String issueAccessToken(Long id, Role role) {
        return issue(id, role, jwtConfiguration.getValidation().getAccess());
    }

    // Refresh 토큰 생성
    public String issueRefreshToken(Long id, Role role) {
        return issue(id, role, jwtConfiguration.getValidation().getRefresh());
    }

    // jwtToken 생성
    private String issue(Long id, Role role, Long expTime) {
        return Jwts.builder()
                .subject(id.toString())
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + expTime))
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    // 검증 메서드
    public boolean validate(String token) {

        try {
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch ( JwtException e ) {
            log.error("token = {}", token);
            log.error("JWT 토큰에 문제가 있습니다.");

        } catch ( IllegalStateException e ) {
            log.error("token = {}", token);
            log.error("이상한 토큰이 검출되었습니다.");

        } catch (Exception e) {
            log.error("token = {}", token);
            log.error(";;");
        }

        return false;

    }

    //토큰 내부 정보를 파싱해서 사용자 ID (sub)와 역할 (role)을 꺼내는 메서드
    /*
        파싱: 문자열로 된 토큰을 프로그램이 다룰 수 있는 구조화된 데이터로 해석해서 꺼내는 과정
     */
    public TokenBody parseJwt(String token) {

        Jws<Claims> parsed = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token);

        String sub = parsed.getPayload().getSubject();
        String role = parsed.getPayload().get("role").toString();

        return new TokenBody(
                Long.parseLong(sub)
                // role이라는 커스텀 Claim를 가져온다.
                , Role.valueOf(role)
        );

    }

    // 시크릿 키 생성
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtConfiguration.getSecrets().getAppKey().getBytes());
    }

    /*
    “클라이언트가 보낸 refresh 토큰 문자열 1개”를 가지고 Access 토큰만 재발급해 주는 로직이고, 중간에 여러 단계의 방어(blacklist/만료/DB 일치)를 걸어 둔 구조
     return: 반환값은 새로 발급된 accessToken 문자열이다.
     */
    @Transactional(readOnly = true) // 이 메서드가 DB 데이터를 조회만 한다는 의미로 붙였습니다.(저장/갱신 없음)
    public String reissueAccessTokenByRefresh(String refreshTokenStr) {

        // 1) DB에 refresh가 있는지 확인.
        // 요청으로 들어온 refreshTokenStr이 DB에 저장된 refresh 토큰인지 먼저 확인합니다.
        // DB에 없으면 “서버가 발급/관리하는 토큰이 아님” → 위조/이미 삭제된 토큰일 수 있으니 재발급을 거절합니다.
        RefreshToken saved = tokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new JwtException("REFRESH_NOT_FOUND"));

        /*
         2) 블랙리스트인지 확인
         - DB에 존재하더라도, 로그아웃 등으로 폐기(revoke) 처리된 refresh 토큰이면 재발급이 되면 안 되니까
         블랙리스트 테이블에 등록되어 있는지 확인하고, 있으면 즉시 거절합니다.
         */
        if (tokenRepository.isBlacklisted(saved)) {
            throw new JwtException("BLACKLISTED_REFRESH_TOKEN");
        }

        /*
         3) refresh JWT 자체 유효성 검사(서명/만료)
         검증 메서드를 통해서 해당 토큰이 유효한지 판단한다.
         실패하면 거절합니다.
         */
        if (!validate(refreshTokenStr)) {
            throw new JwtException("INVALID_REFRESH_TOKEN");
        }

        /*
         4) refresh에서 memberId/role 파싱
         refresh 토큰의 payload에서 memberId(sub)와 role을 꺼냅니다.
         나중에 issueAccessToken(body.id(), body.role())로 access를 만들 때 필요한 재료를 확보하는 단계입니다.
         */
        TokenBody body = parseJwt(refreshTokenStr);

        /*
         5) “현재 유효 refresh”와 일치하는지 재확인 (추가 안전장치)
         DB에 refresh가 교체되었는데, 예전에 발급된 refresh가 다시 들어오는 경우를 막을 수 있습니다.

         “이 회원이 지금 쓰는 refresh가 맞는지”를 마지막으로 잠그는 단계입니다.
         여기서 하는 일은 2가지입니다.
         1. findValidRefToken(memberId)로 “그 회원의 현재 유효한 refresh(블랙리스트 제외)”를 다시 가져옵니다.
         2. 그리고 요청으로 들어온 refresh 문자열과 DB에 있는 ‘현재 refresh’ 문자열이 동일한지 비교합니다.
         */
        RefreshToken current = tokenRepository.findValidRefToken(body.getMemberId())
                .orElseThrow(() -> new JwtException("REFRESH_NOT_FOUND_OR_BLACKLISTED"));

        if (!current.getRefreshToken().equals(refreshTokenStr)) {
            throw new JwtException("REFRESH_MISMATCH");
        }

        // 6) access만 재발급
        // 모든 검증을 통과하면, refresh는 그대로 두고 새 access 토큰만 만들어서 반환
        return issueAccessToken(body.getMemberId(), body.getRole());
    }


}
