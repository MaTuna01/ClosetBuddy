package io.codebuddy.closetbuddy.domain.payments.model.vo;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PaymentResponse(
        @NotNull
        Long paymentAmount,
        @NotNull
        PaymentStatus paymentStatus,
        @NotNull
        LocalDateTime approvedAt,
        @NotNull
        LocalDateTime updatedAt
) {
}
