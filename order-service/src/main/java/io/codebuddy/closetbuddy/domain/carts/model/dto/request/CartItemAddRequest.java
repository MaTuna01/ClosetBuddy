package io.codebuddy.closetbuddy.domain.carts.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemAddRequest(
        @NotNull(message = "상품 Id는 필수입니다.")
        Long productId,
        
        @NotNull(message = "상품 수량은 필수입니다.")
        @Min(value = 1, message = "상품 수량은 1개 이상이어야합니다.")
        Integer productCount
) {
}
