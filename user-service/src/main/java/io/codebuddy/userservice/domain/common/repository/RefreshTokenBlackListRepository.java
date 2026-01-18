package io.codebuddy.userservice.domain.common.repository;

import io.codebuddy.userservice.domain.common.model.entity.RefreshTokenBlackList;import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenBlackListRepository extends JpaRepository<RefreshTokenBlackList, Long> {


    //블랙리스트 테이블에 해당 refresh가 등록돼 있는지를 빠르게 확인하기 위한 exists... 조회 메서드 2개
    /*
       RefreshTokenBlackList 테이블에 refresh_token_id가 이 값인 행이 존재하냐를 확인
       사용시점: refreshToken.getId()를 이미 알고 있을 때 블랙리스트 여부를 확인
     */

    boolean existsByRefreshToken_Id(Long refreshTokenId);
    boolean existsByRefreshToken_RefreshToken(String refreshToken);


}
