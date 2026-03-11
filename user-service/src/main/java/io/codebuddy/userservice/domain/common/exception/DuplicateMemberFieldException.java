package io.codebuddy.userservice.domain.common.exception;

import lombok.Getter;

/*
회원가입 시 중복된 필드값을 체크하기 위한 사용자 정의 예외

field: "memberId", "email", "phone"
value: 실제 중복된 값

사용자에게 어떤 필드에 어떤 값이 중복됐는지 정확히 사용자에게 알려줄 수 있음
 */
@Getter
public class DuplicateMemberFieldException extends RuntimeException{

    private final String field;

    private final String value;

    public DuplicateMemberFieldException(String field, String value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }
}
