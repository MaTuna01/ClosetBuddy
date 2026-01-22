package io.codebuddy.closetbuddy.domain.orders.model.dto.response;

public record OrderItemDto(
        Long productId,
        String storeName,
        String productName,
        Integer orderCount,
        Long orderPrice
) {
}
