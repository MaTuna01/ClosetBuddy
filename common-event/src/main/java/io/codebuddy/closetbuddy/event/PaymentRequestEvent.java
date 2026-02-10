package io.codebuddy.closetbuddy.event;

// 결제 요청 이벤트
public record PaymentRequestEvent(
        Long orderId,
        Long memberId,
        Long amount // 결제 금액
) {
}
