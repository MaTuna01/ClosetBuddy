package io.codebuddy.userservice.domain.auth.form.service;


import io.codebuddy.userservice.domain.common.exception.DuplicateMemberFieldException;
import io.codebuddy.userservice.domain.common.feign.client.OrderServiceClient;
import io.codebuddy.userservice.domain.common.feign.client.PayServiceClient;
import io.codebuddy.userservice.domain.common.feign.dto.AccountCreateRequest;
import io.codebuddy.userservice.domain.common.feign.dto.CartCreateRequest;
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
    private final PayServiceClient payServiceClient;
    private final OrderServiceClient orderServiceClient;



    public Member create(SignReqDTO signReqDTO) {

        // 1) 중복 검사
        if (memberRepository.existsByMemberId(signReqDTO.getMemberId())) {
            throw new DuplicateMemberFieldException(
                    "memberId",
                    signReqDTO.getMemberId(),
                    "이미 사용 중인 아이디입니다."
            );
        }

        if (memberRepository.existsByEmail(signReqDTO.getEmail())) {
            throw new DuplicateMemberFieldException(
                    "email",
                    signReqDTO.getEmail(),
                    "이미 사용 중인 이메일입니다."
            );
        }

        if (memberRepository.existsByPhone(signReqDTO.getPhone())) {
            throw new DuplicateMemberFieldException(
                    "phone",
                    signReqDTO.getPhone(),
                    "이미 사용 중인 전화번호입니다."
            );
        }

        // 2) 통과하면 저장
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
            payServiceClient.createAccount(new AccountCreateRequest(loginmember.getId()));
        } catch (Exception e) {

            throw new RuntimeException("계좌 생성 실패: " + e.getMessage());

        }

        // Order-Service로 장바구니 생성 요청 (동기 통신)
        try {
            orderServiceClient.createMemberCart(new CartCreateRequest(loginmember.getId()).memberId());
        } catch (Exception e) {
            throw new RuntimeException("장바구니 생성 실패: " + e.getMessage());
        }


        return savedMember;
    }


}
