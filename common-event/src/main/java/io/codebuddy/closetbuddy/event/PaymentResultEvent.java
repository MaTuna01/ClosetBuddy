package io.codebuddy.closetbuddy.event;

// 결제 결과 반환 이벤트 DTO
public record PaymentResultEvent(
        Long orderId,
        boolean success,
        String failReason
) {
}