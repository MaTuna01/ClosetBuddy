package io.codebuddy.payservice.domain.pay.payments.model.vo;

import java.time.LocalDateTime;

public record PaymentResponse(

        Long paymentAmount,

        PaymentStatus paymentStatus,

        LocalDateTime approvedAt,

        LocalDateTime updatedAt
) {
}