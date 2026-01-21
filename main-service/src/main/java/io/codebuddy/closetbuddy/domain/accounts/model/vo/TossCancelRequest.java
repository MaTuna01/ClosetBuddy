package io.codebuddy.closetbuddy.domain.accounts.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TossCancelRequest(
        @NotBlank(message = "취소 사유는 필수입니다.")
        @Size(min=2, max=200)
        String cancelReason
) {
}