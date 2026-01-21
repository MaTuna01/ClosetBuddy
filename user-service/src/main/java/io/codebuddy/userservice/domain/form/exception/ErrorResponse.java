package io.codebuddy.userservice.domain.form.exception;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/*
아래와 같은 형식으로 사용자에게 보여주기 위해 변수들을 선언해놓은 모듈
{
  "code": "INVALID_INPUT_VALUE",
  "message": "입력값이 유효하지 않습니다.",
  "errors": [
    {
      "field": "password",
      "value": "1234",
      "reason": "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 모두 포함해야 합니다."
    },
    {
      "field": "email",
      "value": "wrong-email",
      "reason": "올바른 이메일 형식이 아닙니다."
    }
  ]
}
 */
@Getter
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private List<FieldErrorDetail> errors;

    @Getter
    @Builder
    public static class FieldErrorDetail {
        private String field;
        private String value;
        private String reason;
    }
}
