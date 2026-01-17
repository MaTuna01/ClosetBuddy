package io.codebuddy.closetbuddy.domain.orders.dto.response;

public record OrderItemDto(
        Long productId,
        String storeName,
        String productName,
        Integer orderCount,
        Long orderPrice
) {
}
