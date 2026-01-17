package io.codebuddy.closetbuddy.domain.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// jwt 토큰에서 멤버 아이디랑 권한을 가져오기 위한 dto
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenBody {
    private Long memberId;
    private Role role;
}