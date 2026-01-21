package io.codebuddy.closetbuddy.domain.accounts.model.vo;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TossPaymentConfirm(

        String paymentKey,

        String orderId,

        Long amount

) {
}
