package io.codebuddy.userservice.domain.common.repository;


import io.codebuddy.userservice.domain.common.model.entity.RefreshToken;import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    //특정 회원의 refresh 토큰들을 전부 삭제합니다.
    void deleteAllByMember_Id(Long memberId);

    // RefreshToken.member.id = :memberId 조건으로 refresh 토큰 1개를 조회
    Optional<RefreshToken> findByMember_Id(Long memberId);

    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
