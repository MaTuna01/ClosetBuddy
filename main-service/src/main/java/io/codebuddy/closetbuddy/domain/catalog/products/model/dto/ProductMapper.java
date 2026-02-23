package io.codebuddy.closetbuddy.domain.catalog.products.model.dto;

import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.ProductDocument;

public class ProductMapper {
    public static ProductDocument toProductDocument(Product product){
        return new ProductDocument(

                String.valueOf(product.getProductId()),
                product.getProductName(),
                product.getProductPrice(),
                product.getProductStock(),
                String.valueOf(product.getStore().getStoreName()),
                String.valueOf(product.getCategory().getParent().getCode()),
                String.valueOf(product.getCategory().getCode()),
                product.getImageUrl()
        );

    }
}
