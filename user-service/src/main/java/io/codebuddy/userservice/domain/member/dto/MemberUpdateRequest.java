package io.codebuddy.userservice.domain.member.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 회원가입할 때 적용한 규칙들 회원 정보 수정 부분 DTO에도 적용
public record MemberUpdateRequest(
        @Size(min = 1, message = "이름은 한 글자 이상 입력해야 합니다.")
        String name,
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                message = "올바른 이메일 형식(예: user@naver.com)이 아닙니다.")
        String email,
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$",
                message = "올바른 전화번호 형식(010-0000-0000)이 아닙니다.")
        String phone,

        @Size(min = 1, message = "주소는 빈 값일 수 없습니다.")
        String address
) {
}
