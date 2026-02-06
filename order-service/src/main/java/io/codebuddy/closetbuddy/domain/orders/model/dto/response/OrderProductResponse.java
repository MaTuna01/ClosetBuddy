package io.codebuddy.closetbuddy.domain.orders.model.dto.response;

// Feign Client를 위한 Dto
public record OrderProductResponse(
        Long productId,
        String storeName,
        String productName,
        Long productPrice
) {
}
