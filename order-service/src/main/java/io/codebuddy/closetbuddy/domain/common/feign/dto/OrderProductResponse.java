package io.codebuddy.closetbuddy.domain.common.feign.dto;

// Feign Client를 위한 Dto
public record OrderProductResponse(
        Long productId, // 상품 아이디
        String productName, // 상품 이름
        Long sellerId, // 판매자 아이디
        String sellerName, // 판매자 이름
        Long storeId, // 상점 아이디
        String storeName, // 상점 이름
        Long productPrice // 상품 가격
) {
}
