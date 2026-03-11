package io.codebuddy.closetbuddy.event;

import java.util.List;

// 결제 요청 이벤트 DTO
public record PaymentRequestEvent(
        Long orderId,
        Long memberId,
        Long orderAmount,
        List<OrderItemRequest> orderItem
) {
    public record OrderItemRequest(
            Long orderItemId,
            Long sellerId,
            Long storeId,
            Long productId,
            Integer orderCount,
            Long orderPrice,
            String productName,
            String storeName,
            String sellerName
    ) {
    }
}