package io.codebuddy.payservice.domain.pay.accounts.model.vo;

public record TossPaymentConfirm(
        String paymentKey,

        String orderId,

        Long amount
) {
}
