package org.dev.orderservice.domain.orders.model.dto.response;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemCreateRequestDto(
        @NotNull(message = "상품 ID는 필수입니다.")
        Long productId,

        @Min(value = 1, message = "주문 수량은 최소 1개 이상이어야합니다.")
        Integer orderCount
) {
}
