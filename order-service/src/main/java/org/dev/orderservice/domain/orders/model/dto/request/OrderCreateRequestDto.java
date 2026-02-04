package org.dev.orderservice.domain.orders.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// 주문을 생성하기 위한 Dto
public record OrderCreateRequestDto(
        @NotEmpty(message = "주문할 상품 목록은 필수입니다.")
        @Valid // 리스트 내의 요소들을 검증합니다.
        List<OrderItemCreateRequestDto> orderItems
) {
}
