package io.codebuddy.closetbuddy.domain.catalog.products.model.dto;

import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;

// Order-Service에서 사용할 DTO 정의
public record InternalProductResponse(
        Long productId,
        String productName, // 상품 이름
        Long productPrice, // 상품 가격
        int productStock, // 상품 재고
        Long storeId, // 상점 아이디
        String storeName, // 상품 이름
        Long sellerId, // 판매자 아이디
        String sellerName // 판매자 이름
) {
    public static InternalProductResponse from(Product product) {
        return new InternalProductResponse(
                product.getProductId(),
                product.getProductName(),
                product.getProductPrice(),
                product.getProductStock(),
                product.getStore().getId(),
                product.getStore().getStoreName(),
                product.getStore().getSeller().getSellerId(),
                product.getStore().getSeller().getSellerName()
        );
    }
}
