package io.codebuddy.userservice.domain.common.model.dto;

import lombok.Getter;

@Getter
public class UserReqDTO {

    private String username;
    private String memberId;
    private String email;
    private String password;
    private String address;
    private String phone;

}
