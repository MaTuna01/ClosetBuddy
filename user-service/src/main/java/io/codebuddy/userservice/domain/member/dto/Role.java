package io.codebuddy.userservice.domain.member.dto;

import lombok.Getter;

//권한 정의
@Getter
public enum Role {
    ADMIN,
    MEMBER,
    SELLER,
    GUEST
}
