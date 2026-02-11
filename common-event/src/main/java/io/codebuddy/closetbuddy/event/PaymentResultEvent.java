package io.codebuddy.closetbuddy.event;

// 결제 결과 반환 DTO
public record PaymentResultEvent(
        Long orderId,
        boolean success,
        Long paymentId,
        String failReason
) {
}
