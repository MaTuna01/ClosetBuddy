package io.codebuddy.closetbuddy.domain.common.repository;

import io.codebuddy.closetbuddy.domain.common.model.entity.RefreshTokenBlackList;
import io.codebuddy.closetbuddy.domain.common.model.entity.RefreshToken;
import io.codebuddy.closetbuddy.domain.common.model.entity.Member;

import java.util.Optional;


public interface TokenRepository {
    // 특정 회원에 대해 refresh 토큰을 db에 저장 또는 갱신한다. 회원당 refresh 1개 인거면 기존 토큰을 업데이트하는 upsert 성격이 된다.
    RefreshToken save(Member member, String token);
    /*
        특정 refresh 토큰 엔티티를 블랙리스트 테이블에 등록한다.
        void로 해준 이유: 해당 refresh 토큰이 블랙리스트에 저장이 되는 경우 반환이 필요없기 때문
     */
    void addBlackList(RefreshToken refreshToken);

    /*
        memberId 기준으로 유효한 refresh 토큰을 조회한다. 여기에서 토큰이 유효하다는 말은
        refresh 토큰이 존재하고 블랙리스트에 등록되지 않는 것을 의미하도록 구현한다.
     */
    Optional<RefreshToken> findValidRefToken(Long memberId);
    /*
        refresh 토큰 엔티티를 삭제
        회원 탈퇴, refresh 무효화 정책에서 사용
     */
    void delete(RefreshToken refreshToken);

    /*
        클라이언트가 보낸 문자열로 DB의 RefreshToken 엔티티를 찾는다.
        재발급 API에서 요청으로 들어온 refresh가 서버가 발급/관리하는 토큰인지 확인할 때 필요
     */
    Optional<RefreshToken> findByToken(String refreshToken);

    /*
        전달받은 RefreshToken 엔티티가 블랙리스트에 등록되어있는지를 확인
        재발급 로직 초반에 자주 호출.
     */

    boolean isBlacklisted(RefreshToken refreshToken);
//    boolean isBlacklisted(String refreshToken);
}
