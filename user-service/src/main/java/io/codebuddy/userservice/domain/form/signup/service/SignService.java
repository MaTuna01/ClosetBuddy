package io.codebuddy.userservice.domain.form.signup.service;


import io.codebuddy.userservice.domain.common.model.dto.Role;
import io.codebuddy.userservice.domain.common.model.dto.UserReqDTO;
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
