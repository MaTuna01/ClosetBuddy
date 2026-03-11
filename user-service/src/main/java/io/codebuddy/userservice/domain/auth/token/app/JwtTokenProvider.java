package io.codebuddy.userservice.domain.auth.token.app;

import io.codebuddy.userservice.domain.common.config.JwtConfiguration;
import io.codebuddy.userservice.domain.member.dto.Role;
import io.codebuddy.userservice.domain.auth.token.dto.TokenBody;
import io.codebuddy.userservice.domain.auth.token.dto.TokenPair;
import io.codebuddy.userservice.domain.member.domain.Member;
import io.codebuddy.userservice.domain.auth.token.domain.RefreshToken;
import io.codebuddy.userservice.domain.auth.token.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // 기존 검증 메서드 코드의 단점: false만 리턴하면, 호출한 쪽(JwtAuthenticationFilter)은 왜 실패했는지 알 방법이 없음.
    public void validate(String token) {

        Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token);
        // 예외가 발생하지 않으면 정상 통과

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

}
