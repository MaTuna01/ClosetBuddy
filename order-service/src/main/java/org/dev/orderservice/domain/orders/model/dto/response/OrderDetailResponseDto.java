package org.dev.orderservice.domain.orders.model.dto.response;


import org.dev.orderservice.global.config.enumfile.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 상세 내역을 반환합니다.
 */
public record OrderDetailResponseDto(
        Long orderId,
        LocalDateTime createdAt,
        OrderStatus orderStatus,
        Long orderAmount,
        List<OrderItemDto> orderItems
) {
}
