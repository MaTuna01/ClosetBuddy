package io.codebuddy.closetbuddy.domain.orders.dto.response;

import io.codebuddy.closetbuddy.domain.carts.entity.CartItem;

public record CartGetResponseDto(
        Long cartItemId,
        Long productId,
        String productName,
        Integer cartCount,
        Long cartPrice
) {

    /**
     * Entity -> Dto 변환 로직
     */
    public CartGetResponseDto(CartItem entity) {
        this(
                entity.getId(),
                entity.getProduct().getProductId(),
                entity.getProduct().getProductName(),
                entity.getCartCount(),
                entity.getCartPrice()
        );
    }
}
