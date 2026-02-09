package io.codebuddy.closetbuddy.domain.catalog.products.model.dto;

import io.codebuddy.closetbuddy.domain.catalog.category.model.entity.Category;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.ProductDocument;

public record ProductResponse(
        Long productId,
        String productName,
        Long productPrice,
        int productStock,
        String categoryCode,      // 조회시 순환참조가 발생하지 않도록 Entity가 아닌 필드를 반환
        String categoryName,      // 카테고리명
        String parentCategoryCode, // 상위 카테고리 코드
        String storeName
) {

    //엔티티를 DTO로 변환해주는 편의 메서드
    public static ProductResponse from(Product product) {
        Category category = product.getCategory();
        return new ProductResponse(
                product.getProductId(),
                product.getProductName(),
                product.getProductPrice(),
                product.getProductStock(),
                category != null ? category.getCode() : null,
                category != null ? category.getName() : null,
                category != null && category.getParent() != null ? category.getParent().getCode() : null,
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
