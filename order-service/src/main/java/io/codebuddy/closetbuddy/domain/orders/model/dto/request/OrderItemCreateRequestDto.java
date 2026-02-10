package io.codebuddy.closetbuddy.domain.orders.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// 주문 상품 생성 Dto
public record OrderItemCreateRequestDto(
        @NotNull(message = "상품 ID는 필수입니다.")
        Long productId,

        @NotNull(message = "주문 수량은 필수입니다.")
        @Min(value = 1, message = "주문 수량은 최소 1개 이상이어야합니다.")
        Integer orderCount
) {
}
