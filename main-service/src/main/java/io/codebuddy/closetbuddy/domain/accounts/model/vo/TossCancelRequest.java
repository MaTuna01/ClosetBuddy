package io.codebuddy.closetbuddy.domain.accounts.model.vo;

import jakarta.validation.constraints.NotBlank;

public record TossCancelRequest(
        @NotBlank(message = "취소 사유는 필수입니다.")
        String cancelReason
) {
}