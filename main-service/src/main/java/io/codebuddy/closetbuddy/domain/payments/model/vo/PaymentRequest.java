package io.codebuddy.closetbuddy.domain.payments.model.vo;

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
