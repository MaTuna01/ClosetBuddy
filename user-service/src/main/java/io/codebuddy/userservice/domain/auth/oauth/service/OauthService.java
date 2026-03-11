package io.codebuddy.userservice.domain.auth.oauth.service;

import io.codebuddy.userservice.domain.common.feign.client.PayServiceClient;
import io.codebuddy.userservice.domain.common.feign.dto.AccountCreateRequest;
import io.codebuddy.userservice.domain.member.dto.Role;
import io.codebuddy.userservice.domain.member.domain.Member;
import io.codebuddy.userservice.domain.member.repository.MemberRepository;
import io.codebuddy.userservice.domain.auth.token.security.principal.MemberDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.*;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OauthService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final PayServiceClient payServiceClient;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("oAuth2User = {}", oAuth2User);

        // Google 기준: email, name 키가 보통 이렇게 옴
        String email = (String) oAuth2User.getAttributes().get("email");
        String name  = (String) oAuth2User.getAttributes().get("name");

        if (email == null) {
            // email이 없으면 우리 시스템에서 회원 식별이 불가능하니 예외 처리
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        /* 기존의 문제점: orElseGet() 람다 내부에서만 생성되지만 외부에서 구분이 불가능 하다 -> 기존 회원도 매번 계좌 생성 요청*/
        // 기존 회원 조회
        Optional<Member> optionalMember = memberRepository.findByEmail(email);

        boolean isNewMember = optionalMember.isEmpty();
        if (optionalMember.isEmpty()) {
            // 신규 회원 생성
            Member newMember = Member.builder()
                    .username(name)
                    .memberId(email)
                    .email(email)
                    .password("OAUTH_USER")
                    .role(Role.MEMBER)
                    .build();
            memberRepository.save(newMember);
            optionalMember = Optional.of(newMember);
        }

        // Optional에서 실제 Member 객체 꺼내기
        Member member = optionalMember.get();
        MemberDetails memberDetails = new MemberDetails(member, oAuth2User.getAttributes());

        // Main-Service로 계좌 생성 요청 (동기 통신)
        // "신규회원일 때만" 계좌 생성
        if (isNewMember) {
            try {
                payServiceClient.createAccount(new AccountCreateRequest(member.getId()));
                log.info("Account created for new member: {}", member.getId());
            } catch (Exception e) {
                log.warn("PayService account creation failed : memberId={}, error={}",
                        member.getId(), e.getMessage(), e);
            }
        }

        return memberDetails;
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    public Member getById(Long id) {
        return findById(id)
                .orElseThrow(NoSuchElementException::new);
    }

    public MemberDetails getMemberDetailsById(Long id) {
        Member findMember = getById(id);
        return new MemberDetails(findMember);
    }


}
