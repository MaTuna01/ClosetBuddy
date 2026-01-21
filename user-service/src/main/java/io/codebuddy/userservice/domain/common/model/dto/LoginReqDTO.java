package io.codebuddy.userservice.domain.common.model.dto;

import lombok.Getter;

@Getter
public class LoginReqDTO {
    private String memberId;
    private String password;
}
