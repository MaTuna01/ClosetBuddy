package io.codebuddy.closetbuddy.domain.orders.model.dto.response;

import java.util.List;

// 주문 조회용 DTO
public record OrderResponseDto(
        Long orderId,
        List<OrderItemDto> orderItemDto,
        Long orderAmount
) {
}
