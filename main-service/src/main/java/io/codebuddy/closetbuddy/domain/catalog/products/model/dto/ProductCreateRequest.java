package io.codebuddy.closetbuddy.domain.catalog.products.model.dto;

import io.codebuddy.closetbuddy.domain.catalog.category.model.entity.Category;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.catalog.stores.model.entity.Store;
import jakarta.validation.constraints.*;

public record ProductCreateRequest(
        @NotBlank(message = "상품명은 필수입니다.")
        @Size(min = 1, max = 50, message = "상품명은 1글자 이상 50자 미만이어야합니다.")
        String productName,

        @NotNull(message = "상품 가격은 필수입니다.")
        @Min(value = 0, message = "가격은 최소 0원 이상이어야 합니다.")
        @Max(value = 1000000000, message = "상품 가격의 상한선은 10억원 입니다.")
        Long productPrice,

        @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
        int productStock,

        @Size(min = 3, max = 999999, message = "Url 경로 길이를 확인하세요")
        String imgUrl,

        @NotNull(message = "카테고리는 필수 항목입니다.")
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
