package io.codebuddy.closetbuddy.domain.accounts.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentConfirmRequest(
        @NotBlank
        String paymentKey, // PG사 결제 키

        @NotBlank
        String orderId,    // PG사 거래 번호

        @NotNull
        @Positive
        Long amount        // 충전 금액
) {
}
