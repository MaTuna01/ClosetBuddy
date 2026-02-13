package io.codebuddy.payservice.domain.common.feign.dto;

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
}