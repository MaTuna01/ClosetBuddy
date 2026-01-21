package io.codebuddy.closetbuddy.domain.accounts.model.vo;

import jakarta.validation.constraints.*;

public record PaymentConfirmRequest(
        @NotBlank(message = "paymentKey는 필수 입력 값입니다.")
        @Max(value = 200, message = "paymentKey는 최대 200자입니다.")
        String paymentKey, // PG사 결제 키

        @NotBlank(message = "orderId는 필수 입력 값입니다.")
        @Min(value = 6, message = "orderId는 최소 6자입니다.")
        @Max(value = 64, message = "orderId는 최대 64자입니다.")
        String orderId,    // PG사 거래 번호

        @NotBlank(message = "가격은 필수 입력 값입니다.")
        @Min(1)
        Long amount        // 충전 금액
) {
}
