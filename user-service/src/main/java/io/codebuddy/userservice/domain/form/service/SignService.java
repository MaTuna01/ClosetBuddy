package io.codebuddy.userservice.domain.form.service;


import io.codebuddy.userservice.domain.common.model.dto.Role;
import io.codebuddy.userservice.domain.common.model.dto.SignReqDTO;
import io.codebuddy.userservice.domain.common.model.entity.Member;
import io.codebuddy.userservice.domain.common.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SignService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


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


        return memberRepository.save(loginmember);
    }


}
