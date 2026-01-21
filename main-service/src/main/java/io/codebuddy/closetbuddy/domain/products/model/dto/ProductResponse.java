package io.codebuddy.closetbuddy.domain.products.model.dto;

import io.codebuddy.closetbuddy.domain.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.stores.model.entity.Store;

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
                        //!= null ? product.getCategory().name() : "Uncategorized",
                product.getStore().getStoreName()
        );
    }
}
