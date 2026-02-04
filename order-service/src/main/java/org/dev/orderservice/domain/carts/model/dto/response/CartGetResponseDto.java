package org.dev.orderservice.domain.carts.model.dto.response;


import org.dev.orderservice.domain.carts.model.entity.CartItem;

public record CartGetResponseDto(
        Long cartItemId,
        Long productId,
        String productName,
        Integer cartCount
) {

    /**
     * Entity -> Dto 변환 로직
     */
    public CartGetResponseDto(CartItem entity) {
        this(
                entity.getId(),
                entity.getProductId(),
                entity.getProductName(),
                entity.getCartCount()
        );
    }
}
