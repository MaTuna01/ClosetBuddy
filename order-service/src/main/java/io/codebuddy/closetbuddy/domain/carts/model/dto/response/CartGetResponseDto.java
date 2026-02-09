package io.codebuddy.closetbuddy.domain.carts.model.dto.response;


import io.codebuddy.closetbuddy.domain.carts.model.entity.CartItem;
import io.codebuddy.closetbuddy.domain.common.feign.dto.CartProductResponse;

// 장바구니 조회를 위한 Dto
public record CartGetResponseDto(
        Long cartItemId,
        Long productId,
        String productName,
        Long productPrice,
        Integer cartCount,
        String storeName
) {
    // Entity -> Dto 변환 로직
    public static CartGetResponseDto of(CartItem cartItem, CartProductResponse productResponse) {
        return new CartGetResponseDto(
                cartItem.getId(),
                cartItem.getProductId(),
                productResponse.productName(),
                productResponse.productPrice(),
                cartItem.getCartCount(),
                productResponse.storeName()
        );
    }
}

