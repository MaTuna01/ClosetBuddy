package io.codebuddy.closetbuddy.domain.catalog.products.model.dto;

import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.ProductDocument;

public record ProductResponse(
        Long productId,
        String productName,
        Long productPrice,
        int productStock,
        Category category,
        String storeName
) {

    //엔티티를 DTO로 변환해주는 편의 메서드
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getProductName(),
                product.getProductPrice(),
                product.getProductStock(),
                product.getCategory(),
                product.getStore().getStoreName()
        );
    }

    // document -> productResponse
    public static ProductResponse fromDocument(ProductDocument doc) {
        return new ProductResponse(
                Long.parseLong(doc.getId()),
                doc.getProductName(),
                doc.getProductPrice(),
                doc.getProductStock(),
                Category.valueOf(doc.getCategory()),
                doc.getStoreName()
        );
    }
}
