package io.codebuddy.closetbuddy.domain.carts.model.dto.response;


import io.codebuddy.closetbuddy.domain.carts.model.entity.CartItem;

// 장바구니 조회를 위한 Dto
public record CartGetResponseDto(
        Long productId,
        Long cartItemId,
        String productName, // 상품 이름
        String storeName, // 가게 이름
        String sellerName, // 판매자 이름
        Integer cartCount // 담은 수량
) {
    // Entity -> Dto 변환 로직
    public CartGetResponseDto(CartItem entity) {
        this(
                entity.getProductId(),
                entity.getCart().getCartItems().get(0).getId(),
                entity.getProductName(),
                entity.getStoreName(),
                entity.getSellerName(),
                entity.getCartCount()
        );
    }
}

