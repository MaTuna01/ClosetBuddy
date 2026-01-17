package io.codebuddy.closetbuddy.domain.common.model.dto;

import lombok.Getter;

//권한 정의
@Getter
public enum Role {
    ADMIN,
    MEMBER,
    SELLER,
    GUEST
}
