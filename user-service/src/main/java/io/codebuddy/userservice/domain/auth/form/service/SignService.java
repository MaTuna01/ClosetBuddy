package io.codebuddy.userservice.domain.auth.form.service;


import io.codebuddy.userservice.domain.common.feign.client.MainServiceClient;
import io.codebuddy.userservice.domain.common.feign.dto.AccountCreateRequest;
import io.codebuddy.userservice.domain.member.dto.Role;
import io.codebuddy.userservice.domain.auth.token.dto.SignReqDTO;
import io.codebuddy.userservice.domain.member.domain.Member;
import io.codebuddy.userservice.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class SignService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MainServiceClient mainServiceClient;


    public Member create(SignReqDTO signReqDTO) {

        Member loginmember = Member.builder()
                .username(signReqDTO.getUsername())
                .memberId(signReqDTO.getMemberId())
                .email(signReqDTO.getEmail())
                .password(passwordEncoder.encode(signReqDTO.getPassword()))
                .address(signReqDTO.getAddress())
                .phone(signReqDTO.getPhone())
                .role(Role.MEMBER)
                .build();

        Member savedMember = memberRepository.save(loginmember);

        // Main-Service로 계좌 생성 요청 (동기 통신)
        try {
            mainServiceClient.createAccount(new AccountCreateRequest(loginmember.getId()));
        } catch (Exception e) {

            throw new RuntimeException("계좌 생성 실패: " + e.getMessage());

        }


        return savedMember;
    }


}
