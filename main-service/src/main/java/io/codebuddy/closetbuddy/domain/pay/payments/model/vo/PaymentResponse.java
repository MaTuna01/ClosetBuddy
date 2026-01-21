package io.codebuddy.closetbuddy.domain.pay.payments.model.vo;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PaymentResponse(

        Long paymentAmount,

        PaymentStatus paymentStatus,

        LocalDateTime approvedAt,

        LocalDateTime updatedAt
) {
}
