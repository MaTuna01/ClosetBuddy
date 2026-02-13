package io.codebuddy.payservice.domain.common.feign.dto;

import java.util.List;

public record InternalOrderResponse(
        Long orderAmount,
        List<InternalOrderItemResponse> orderItem
) {
}
