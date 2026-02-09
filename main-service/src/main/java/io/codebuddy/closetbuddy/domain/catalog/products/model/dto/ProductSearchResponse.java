package io.codebuddy.closetbuddy.domain.catalog.products.model.dto;

import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.ProductDocument;

public record ProductSearchResponse(
        Long productId,
        String productName,
        Long productPrice,
        int productStock,
        String categoryName,      // 카테고리명
        String storeName
) {
    // document -> productResponse
    public static ProductSearchResponse fromDocument(ProductDocument doc) {
        return new ProductSearchResponse(
                Long.parseLong(doc.getId()),
                doc.getProductName(),
                doc.getProductPrice(),
                doc.getProductStock(),
                doc.getCategoryName(),
                doc.getStoreName()
        );
    }
}
