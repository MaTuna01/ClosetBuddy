package io.codebuddy.closetbuddy.domain.pay.payments.model.vo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull(message = "결제할 주문 번호는 필수 입력 값입니다.")
        Long orderId,

        @NotNull(message = "결제 금액은 필수 입력 값입니다.")
        @Min(0)
        Long amount
) {
}
