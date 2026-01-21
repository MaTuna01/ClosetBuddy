package io.codebuddy.closetbuddy.domain.pay.payments.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PaymentRequest(
        @NotBlank(message = "결제할 주문 번호는 필수 입력 값입니다.")
        Long orderId,

        @NotBlank(message = "결제 금액은 필수 입력 값입니다.")
        @Positive
        Long amount
) {
}
