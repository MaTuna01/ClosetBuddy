package io.codebuddy.closetbuddy.event;

// 재고 확인 결과 반환 DTO
public record StockCheckResult(
        Long orderId,
        boolean success,
        String failReason
) {
}
