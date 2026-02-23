package io.codebuddy.closetbuddy.domain.catalog.products.model.dto;

import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;

// Order-Service 내의 추천 시스템에 보낼 DTO 정의
public record InternalRecommendProductResponse(
        Long productId,
        String productName,
        Long productPrice,
        String imageUrl,
        String storeName,
        String categoryCode
) {
    public static InternalRecommendProductResponse from(Product product) {
        return new InternalRecommendProductResponse(
                product.getProductId(),
                product.getProductName(),
                product.getProductPrice(),
                product.getImageUrl(),
                product.getStore().getStoreName(),
                product.getCategory().getCode()
        );
    }
}
