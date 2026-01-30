package io.codebuddy.closetbuddy.domain.pay.accounts.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TossCancelRequest(
        @NotBlank(message = "취소 사유는 필수입니다.")
        @Size(min=2, max=200, message = "취소 사유는 2자~200자 입니다.")
        String cancelReason
) {
}