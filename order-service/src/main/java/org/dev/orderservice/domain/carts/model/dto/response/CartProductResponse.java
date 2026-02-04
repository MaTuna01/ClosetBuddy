package org.dev.orderservice.domain.carts.model.dto.response;

// Feign Client를 위한 Dto
public record CartProductResponse(
        Long productId,
        String productName,
        Long productPrice,
        int productStock,
        String storeName
) {
}
