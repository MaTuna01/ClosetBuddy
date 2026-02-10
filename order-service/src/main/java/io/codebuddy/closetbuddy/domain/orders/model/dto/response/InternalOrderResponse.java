package io.codebuddy.closetbuddy.domain.orders.model.dto.response;

import io.codebuddy.closetbuddy.domain.orders.model.entity.Order;
import io.codebuddy.closetbuddy.global.config.enumfile.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record InternalOrderResponse(
        OrderStatus orderStatus,
        Long orderAmount,
        List<InternalOrderItemResponse> orderItem
) {
    public static InternalOrderResponse from(Order order, List<InternalOrderItemResponse> orderItem){
        return new InternalOrderResponse(
                order.getOrderStatus(),
                order.getOrderAmount(),
                orderItem
        );
    }
}
