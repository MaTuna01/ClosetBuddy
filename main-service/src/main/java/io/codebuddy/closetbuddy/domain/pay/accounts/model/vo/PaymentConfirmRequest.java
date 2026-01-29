package io.codebuddy.closetbuddy.domain.pay.accounts.model.vo;

import jakarta.validation.constraints.*;

public record PaymentConfirmRequest(
        @NotBlank(message = "paymentKey는 필수 입력 값입니다.")
        @Size(max=200, message = "paymentKey는 최대 200자입니다.")
        String paymentKey, // PG사 결제 키

        @NotBlank(message = "orderId는 필수 입력 값입니다.")
        @Size(min=6, max=64, message = "orderId는 6~62자 입니다.")
        String orderId,    // PG사 거래 번호

        @NotNull(message = "금액은 필수입니다.")
        Long amount        // 충전 금액
) {
}
