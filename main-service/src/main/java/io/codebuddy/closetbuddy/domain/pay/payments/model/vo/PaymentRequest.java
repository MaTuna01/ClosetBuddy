package io.codebuddy.closetbuddy.domain.pay.payments.model.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentRequest(
        @NotNull
        Long orderId,

        @NotNull
        @Positive
        Long amount
) {
}
