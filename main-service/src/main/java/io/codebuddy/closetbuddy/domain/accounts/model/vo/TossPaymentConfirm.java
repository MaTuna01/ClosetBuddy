package io.codebuddy.closetbuddy.domain.accounts.model.vo;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TossPaymentConfirm(
        @NotBlank
        String paymentKey,

        @NotBlank
        String orderId,

        @NotNull
        @Positive
        Long amount

) {
}
