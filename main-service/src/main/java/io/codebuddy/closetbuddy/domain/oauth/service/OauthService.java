package io.codebuddy.closetbuddy.domain.oauth.service;

import io.codebuddy.closetbuddy.domain.common.model.dto.Role;
import io.codebuddy.closetbuddy.domain.common.security.auth.MemberDetails;
import io.codebuddy.closetbuddy.domain.common.repository.MemberRepository;
import io.codebuddy.closetbuddy.domain.common.model.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OauthService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

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

        // 기존 회원이면 조회, 없으면 생성
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .username(name)
                                .memberId(email)
                                .email(email)
                                .password("OAUTH_USER")
                                .role(Role.MEMBER)
                                .build()
                ));
        return new MemberDetails(member, oAuth2User.getAttributes());
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
