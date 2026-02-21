package io.codebuddy.closetbuddy.domain.common.dto;

public record RecommendProductInfoResponse(
        Long productId,
        String productName,
        Long productPrice,
        String imageUrl,
        String storeName,
        String categoryCode
) {
}
