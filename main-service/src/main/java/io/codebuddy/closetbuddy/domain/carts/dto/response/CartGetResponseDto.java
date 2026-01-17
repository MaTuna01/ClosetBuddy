package io.codebuddy.closetbuddy.domain.carts.dto.response;

import io.codebuddy.closetbuddy.domain.carts.entity.CartItem;

public record CartGetResponseDto(
        String productName,
        Integer cartCount
) {

    /**
     * Entity -> Dto 변환 로직
     */
    public CartGetResponseDto(CartItem entity) {
        this(
                entity.getProduct().getProductName(),
                entity.getCartCount()
        );
    }
}
