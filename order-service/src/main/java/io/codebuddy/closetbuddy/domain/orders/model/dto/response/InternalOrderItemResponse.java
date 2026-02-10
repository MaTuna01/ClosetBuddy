package io.codebuddy.closetbuddy.domain.orders.model.dto.response;

import io.codebuddy.closetbuddy.domain.orders.model.entity.OrderItem;

public record InternalOrderItemResponse(
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
    public static InternalOrderItemResponse from(OrderItem oi){
        return new InternalOrderItemResponse(
                oi.getOrderItemId(),
                oi.getSellerId(),
                oi.getStoreId(),
                oi.getProductId(),
                oi.getOrderCount(),
                oi.getOrderPrice(),
                oi.getProductName(),
                oi.getStoreName(),
                oi.getSellerName()
        );
    }
}
