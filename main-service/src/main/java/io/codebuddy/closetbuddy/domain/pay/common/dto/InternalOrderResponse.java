package io.codebuddy.closetbuddy.domain.pay.common.dto;

import io.codebuddy.closetbuddy.global.config.enumfile.OrderStatus;

import java.util.List;

public record InternalOrderResponse(
        OrderStatus orderStatus,
        Long orderAmount,
        List<InternalOrderItemResponse> orderItem
) {
}
