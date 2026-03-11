package io.codebuddy.closetbuddy.domain.common.feign.dto;

// Feign Client에서 받아오는 Dto
public record CartProductResponse(
        Long productId,
        String productName,
        Long sellerId,
        String sellerName,
        Long storeId,
        String storeName,
        Long productPrice,
        String categoryCode,
        String imageUrl
) {
}
