package io.codebuddy.closetbuddy.domain.carts.model.dto.response;

// Feign Client를 위한 Dto
public record CartProductResponse(
        Long productId,
        String productName,
        Long sellerId,
        String sellerName,
        Long storeId,
        Long productPrice,
        Integer productStock,
        String storeName
) {
}
