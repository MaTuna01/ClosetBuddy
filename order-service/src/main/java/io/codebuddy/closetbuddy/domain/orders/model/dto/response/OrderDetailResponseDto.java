package io.codebuddy.closetbuddy.domain.orders.model.dto.response;



import io.codebuddy.closetbuddy.global.config.enumfile.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

// 주문 상세 내역 반환 Dto
public record OrderDetailResponseDto(
        Long orderId,
        LocalDateTime createdAt,
        OrderStatus orderStatus,
        Long orderAmount,
        List<OrderItemDto> orderItems
) {
}
