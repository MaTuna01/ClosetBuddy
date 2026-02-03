package io.codebuddy.closetbuddy.domain.carts.feign;

public record CartProductResponse(
        Long productId,
        String productName,
        Long productPrice,
        int productStock,
        String storeName
) {
}
