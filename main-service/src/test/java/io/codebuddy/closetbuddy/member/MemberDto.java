package io.codebuddy.closetbuddy.member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberDto {
    private String memberId;
    private String username;
    private String email;
    private String address;
    private String phone;
    private String role;
}