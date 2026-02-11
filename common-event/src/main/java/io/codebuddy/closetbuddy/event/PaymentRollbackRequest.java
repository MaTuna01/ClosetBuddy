package io.codebuddy.closetbuddy.event;

// 결제 환불 요청 DTO
public record PaymentRollbackRequest(
        Long orderId,
        Long memberId,
        Long paymentId
) {
}
