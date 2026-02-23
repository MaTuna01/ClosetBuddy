package io.codebuddy.closetbuddy.domain.catalog.products.model.dto;

import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.ProductDocument;

public class ProductMapper {
    public static ProductDocument toProductDocument(Product product){

        String topCategoryCode = null;
        String subCategoryCode = null;

        if (product.getCategory() != null) {
            if (product.getCategory().getParent() != null) {
                // 하위 카테고리가 등록된 경우
                topCategoryCode = String.valueOf(product.getCategory().getParent().getCode());
                subCategoryCode = String.valueOf(product.getCategory().getCode());
            } else {
                // 부모 카테고리만 등록된 경우
                topCategoryCode = String.valueOf(product.getCategory().getCode());
            }
        }

        return new ProductDocument(

                String.valueOf(product.getProductId()),
                product.getProductName(),
                product.getProductPrice(),
                product.getProductStock(),
                String.valueOf(product.getStore().getStoreName()),
                topCategoryCode,
                subCategoryCode,
                product.getImageUrl()
        );

    }
}
