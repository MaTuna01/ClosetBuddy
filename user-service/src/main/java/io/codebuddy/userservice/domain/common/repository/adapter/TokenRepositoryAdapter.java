package io.codebuddy.userservice.domain.common.repository.adapter;

import io.codebuddy.userservice.domain.common.model.entity.Member;
import io.codebuddy.userservice.domain.common.model.entity.RefreshToken;
import io.codebuddy.userservice.domain.common.model.entity.RefreshTokenBlackList;
import io.codebuddy.userservice.domain.common.repository.RefreshTokenBlackListRepository;
import io.codebuddy.userservice.domain.common.repository.RefreshTokenRepository;
import io.codebuddy.userservice.domain.common.repository.TokenRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

//RefreshTokenRepository와 RefreshTokenBlackListRepository 이 두 저장소를 TokenRepository 형태로 변환해주는 역할을 하는 어댑터이다.
@Repository
@RequiredArgsConstructor
public class TokenRepositoryAdapter implements TokenRepository {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenBlackListRepository refreshTokenBlackListRepository;

    private final EntityManager entityManager;

    /*
    의도: 해당 member의 refresh 토큰이 이미 있으면 가져오고, 없으면 새 엔티티를 만들어 refresh 토큰을 새로 만든 뒤 저장하려는 upsert 형태
    refresh Token이 존재하지 않은 경우와 전달받은 refresh token과 db에 저장된 refresh token이 다른 경우에만 갱신을 하도록 조건문을 추가.
     */
    @Override
    public RefreshToken save(Member member, String token) {
        RefreshToken rt = refreshTokenRepository.findByMember_Id(member.getId())
                .orElseGet(() -> RefreshToken.builder()
                        .member(member)
                        .build());

        // 이미 존재하면 값이 바뀔 때만 갱신
        if (rt.getRefreshToken() == null || !rt.getRefreshToken().equals(token)) {
            rt.rotate(token);
        }

        return refreshTokenRepository.save(rt);
    }

    /*
    해당 refresh 토큰이 블랙리스트에 없으면 블랙리스트 테이블에 저장합니다.
    이미 블랙리스트면 아무것도 안함.
     */
    @Override
    public void addBlackList(RefreshToken refreshToken) {
        if (refreshTokenBlackListRepository.existsByRefreshToken_Id(refreshToken.getId())) {
            return;
        }
        refreshTokenBlackListRepository.save(
                RefreshTokenBlackList.builder()
                        .refreshToken(refreshToken)
                        .build()
        );
    }

    /*
    블랙리스트에 없는 유효 refresh 토큰을 찾는 메서드
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findValidRefToken(Long memberId) {
        return entityManager.createQuery(
                        "select rf from RefreshToken rf left join RefreshTokenBlackList rtb on rtb.refreshToken = rf where rf.member.id = :memberId and rtb.id is null"
                        , RefreshToken.class)
                .setParameter("memberId", memberId)
                .getResultStream()
                .findFirst();
    }

    // 전달받은 refresh 엔티티를 삭제합니다.
    @Override
    public void delete(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }

    /*
    refresh 문자열로 RefreshToken 엔티티를 조회합니다.
    클라이언트가 준 refresh가 DB에 저장된 토큰이 맞는지 확인할 때 사용합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken);
    }

    /*
    refreshToken의 id로 블랙리스트 테이블에 존재하는지 확인합니다.
    재발급 로직 초기에 “폐기된 refresh인지” 판단하는 용도입니다.

     */
    @Override
    @Transactional(readOnly = true)
    public boolean isBlacklisted(RefreshToken refreshToken) {
        return refreshTokenBlackListRepository.existsByRefreshToken_Id(refreshToken.getId());
    }


}