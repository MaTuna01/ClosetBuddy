package io.codebuddy.userservice.domain.common.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignReqDTO {

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private String username;

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하로 입력해주세요.")
    private String memberId;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "올바른 이메일 형식(.com, .net 등 포함)이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
    private String password;

    @NotBlank(message = "주소는 필수 입력 항목입니다.")
    private String address;

    @NotBlank(message = "번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식(010-0000-0000)이 아닙니다.")
    private String phone;

}
