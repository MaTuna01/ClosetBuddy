package io.codebuddy.closetbuddy.domain.orders.model.dto.response;

// 주문 상품 DTO
public record OrderItemDto(
        Long productId,
        String storeName,
        String productName,
        Long orderPrice,
        Integer orderCount
) {
}
