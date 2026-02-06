package io.codebuddy.closetbuddy.domain.carts.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartUpdateRequest(
        @NotNull(message = "변경할 장바구니 상품 ID는 필수입니다.")
        Long cartId,

        @NotNull
        @Min(value = 1, message = "변경할 수량은 1개 이상이어야 합니다.")
        Integer cartCount
) {
}
