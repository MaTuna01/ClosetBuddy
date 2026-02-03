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
            // [디버깅용] 에러 로그를 출력해야 main-service가 무슨 말을 했는지 알 수 있음
            e.printStackTrace();

            // 만약 FeignException이라면 e.contentUTF8() 등을 통해
            // main-service가 보낸 구체적인 에러 메시지(500, Null constraint violation 등)를 볼 수 있어.

            throw new RuntimeException("계좌 생성 실패: " + e.getMessage());
        }


        return savedMember;
    }


}
