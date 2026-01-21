package io.codebuddy.closetbuddy.domain.products.model.dto;

import io.codebuddy.closetbuddy.domain.stores.model.entity.Store;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateProductRequest(
        @NotBlank String productName,
        @NotNull Long productPrice,
        int productStock,
        String imageUrl,
        Category category
) {
}
