package io.codebuddy.closetbuddy.domain.form.signup.service;

import io.codebuddy.closetbuddy.domain.common.model.dto.Role;
import io.codebuddy.closetbuddy.domain.common.model.entity.Member;
import io.codebuddy.closetbuddy.domain.common.repository.MemberRepository;
import io.codebuddy.closetbuddy.domain.common.model.dto.UserReqDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SignService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    public Member create(UserReqDTO userReqDTO) {

        Member loginmember = Member.builder()
                .username(userReqDTO.getUsername())
                .memberId(userReqDTO.getMemberId())
                .email(userReqDTO.getEmail())
                .password(passwordEncoder.encode(userReqDTO.getPassword()))
                .address(userReqDTO.getAddress())
                .phone(userReqDTO.getPhone())
                .role(Role.MEMBER)
                .build();


        return memberRepository.save(loginmember);
    }


}
