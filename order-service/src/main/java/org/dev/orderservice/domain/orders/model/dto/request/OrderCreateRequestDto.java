package org.dev.orderservice.domain.orders.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.dev.orderservice.domain.orders.model.dto.response.OrderItemCreateRequestDto;

import java.util.List;

public record OrderCreateRequestDto(
        @NotEmpty(message = "주문할 상품 목록은 필수입니다.")
        @Valid // 리스트 내의 요소들을 검증합니다.
        List<OrderItemCreateRequestDto> orderItems
) {
}
