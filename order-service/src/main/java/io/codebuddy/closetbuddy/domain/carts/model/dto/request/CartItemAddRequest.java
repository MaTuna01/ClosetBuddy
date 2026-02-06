package io.codebuddy.closetbuddy.domain.carts.model.dto.request;

public record CartItemAddRequest(
        Long productId,
        Integer productCount
) {
}
