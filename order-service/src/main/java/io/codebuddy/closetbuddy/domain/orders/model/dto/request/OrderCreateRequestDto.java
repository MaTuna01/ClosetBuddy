package io.codebuddy.closetbuddy.domain.orders.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// 주문을 생성하기 위한 Dto
public record OrderCreateRequestDto(
        @NotEmpty(message = "주문할 상품 목록은 필수입니다.")
        @Valid
        List<OrderItemCreateRequestDto> orderItems
) {
}
