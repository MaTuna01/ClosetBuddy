package io.codebuddy.userservice;

import io.codebuddy.userservice.domain.common.app.JwtTokenProvider;
import io.codebuddy.userservice.domain.common.model.dto.Role;
import io.codebuddy.userservice.domain.common.model.dto.TokenBody;
import io.codebuddy.userservice.domain.common.model.dto.TokenPair;
import io.codebuddy.userservice.domain.common.model.entity.Member;
import io.codebuddy.userservice.domain.common.model.entity.RefreshToken;
import io.codebuddy.userservice.domain.common.repository.MemberRepository;
import io.codebuddy.userservice.domain.common.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


import static org.assertj.core.api.Assertions.*;

//발급 + refresh upsert
@SpringBootTest // 스프링 애플리케이션 컨텍스트를 올려서 bean을 주입함(JwtTokenProvider, MemberRepository, RefreshTokenRepository 가 실행)
@Transactional //테스트가 끝나면 기본적으로 롤백되어 DB 데이터가 남지 않게(격리) 하려는 의도
public class JwtTokenProviderIntegrationTest {
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired MemberRepository memberRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    /*
    토큰 발급 로직에서 member.getId()가 필요하므로
    Member를 DB에 저장해서 id를 발급받는 준비 단계
     */
    private Member saveMember(String memberId) {
        return memberRepository.save(Member.builder()
                .username(memberId)
                .memberId(memberId)
                .email(memberId + "@test.com")
                .password("pw")
                .address("addr")
                .phone("010-0000-0000")
                .role(Role.MEMBER) // 프로젝트 enum 값에 맞게
                .build());
    }

    /*
     토큰 발급 + refresh 토큰 DB 저장
     */
    @DisplayName("generateTokenPair: access/refresh 발급 + refreshToken DB 저장")
    @Test
    void generateTokenPair_savesRefreshToken() {
        Member member = saveMember("kim01");

        TokenPair pair = jwtTokenProvider.generateTokenPair(member);

        assertThat(pair.getAccessToken()).isNotBlank();
        assertThat(pair.getRefreshToken()).isNotBlank();
        //access/refresh 토큰이 생성이 되는지 확인

        jwtTokenProvider.validate(pair.getAccessToken()); //accessToken이 유효한 JWT인지 확인-> validate에서 예외가 없이 처리가 된다면 토큰 발급 관련 기본 검증이 통과되었다는 뜻
        TokenBody body = jwtTokenProvider.parseJwt(pair.getAccessToken()); //accessToken 내부 claim이 의도대로 들어갔는지 확인
        assertThat(body.getMemberId()).isEqualTo(member.getId());
        assertThat(body.getRole()).isEqualTo(member.getRole());

        // refreshToken이 DB에 저장되었는지 확인
        RefreshToken saved = refreshTokenRepository.findByMember_Id(member.getId()).orElseThrow(); //refresh 엔티티를 가져와
        assertThat(saved.getRefreshToken()).isEqualTo(pair.getRefreshToken()); //DB에 저장된 refresh 문자열이 방금 발급한 refresh와 같은지 검증합니다.
    }

    //“같은 회원에게 refreshToken을 여러 번 발급했을 때 DB에 refresh row가 여러 개 쌓이지 않고, 1개 row를 유지하면서 값만 갱신(upsert) 되는지”를 검증
    @DisplayName("generateTokenPair: 같은 회원이면 refreshToken은 1개로 유지되고 값만 갱신(upsert)된다")
    @Test
    void generateTokenPair_upsert_onePerMember() {
        Member member = saveMember("kim02");

        // generateTokenPair()를 두 번 호출
        TokenPair pair1 = jwtTokenProvider.generateTokenPair(member); //첫 호출 후 DB에 저장된 refresh 엔티티의 id(rtId1)를 저장
        Long rtId1 = refreshTokenRepository.findByMember_Id(member.getId()).orElseThrow().getId();

        TokenPair pair2 = jwtTokenProvider.generateTokenPair(member); //두 번째 호출 후 다시 DB에서 refresh 엔티티를 조회해서
        RefreshToken after = refreshTokenRepository.findByMember_Id(member.getId()).orElseThrow();

        assertThat(after.getId()).isEqualTo(rtId1); //2번째 호출후 얻은 after.getId()가 첫 호출 후 DB에 저장된 refresh 엔티티의 id와 같은지 확인
        assertThat(after.getRefreshToken()).isEqualTo(pair2.getRefreshToken()); //refreshToken 값이 두 번째 발급 토큰으로 바뀌었는지 확인
        assertThat(after.getRefreshToken()).isNotEqualTo(pair1.getRefreshToken()); //첫 번째 refreshToken과는 다른지 확인
    }
}
