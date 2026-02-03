package io.codebuddy.closetbuddy.domain.orders.feign;

public record OrderProductResponse(
        Long productId,
        String storeName,
        String productName,
        Long productPrice
) {
}
