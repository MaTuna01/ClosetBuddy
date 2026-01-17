package io.codebuddy.closetbuddy.domain.products.model.dto;

import io.codebuddy.closetbuddy.domain.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.stores.model.entity.Store;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductCreateRequest(
        @NotBlank(message = "상품명은 필수입니다.")
        String productName,

        @NotNull(message = "상품 가격은 필수입니다.")
        @Min(value = 0, message = "가격은 최소 0원 이상이엉야 합니다.")
        Long productPrice,

        @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
        int productStock,

        String imgUrl,

        Category category
) {
    public Product toEntity(Store store) {
        return Product.builder()
                .productName(this.productName)
                .productPrice(this.productPrice)
                .productStock(this.productStock)
                .imageUrl(this.imgUrl)
                .category(this.category)
                .store(store)
                .build();
    }
}
