package io.codebuddy.closetbuddy.domain.products.model.dto;

import jakarta.validation.constraints.*;

public record UpdateProductRequest(
        @NotBlank (message = "상품명은 필수입니다.")
        String productName,
        @NotNull (message = "상품 가격은 필수입니다.")
        @Min(value = 0, message = "상품 가격은 최소 0원 이상이어야 합니다.")
        @Max(value = 100000000, message = "상품 가격은 최대 10억원 이하입니다.")
        Long productPrice,
        @Min(value = 0, message = "상품 재고는 0개 이상이어야 합니다.")
        int productStock,
        @Size(min = 3, max = 999999, message = "Url 경로 길이를 확인하세요")
        String imageUrl,
        @Size(min = 1, max = 9999, message = "카테고리 이름 길이를 확인하세요(1~9999)")
        Category category
) {
}
