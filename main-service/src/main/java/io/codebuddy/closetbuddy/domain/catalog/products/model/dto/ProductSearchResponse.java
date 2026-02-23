package io.codebuddy.closetbuddy.domain.catalog.products.model.dto;

import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.ProductDocument;

public record ProductSearchResponse(
        Long productId,
        String productName,
        Long productPrice,
        int productStock,
        String topCategory,
        String subCategory,
        String storeName
) {
    // document -> productResponse
    public static ProductSearchResponse fromDocument(ProductDocument doc) {
        return new ProductSearchResponse(
                Long.parseLong(doc.getId()),
                doc.getProductName(),
                doc.getProductPrice(),
                doc.getProductStock(),
                doc.getTopCategory(),
                doc.getSubCategory(),
                doc.getStoreName()
        );
    }
}
