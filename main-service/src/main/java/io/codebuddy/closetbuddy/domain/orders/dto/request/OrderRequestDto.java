package io.codebuddy.closetbuddy.domain.orders.dto.request;

import io.codebuddy.closetbuddy.domain.orders.dto.response.OrderItemDto;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequestDto(
        @NotNull
        Long productName,
        @NotNull
        Integer orderCount,
        @NotNull
        Long orderPrice,
        List<OrderItemDto> orderItemDtoList
) {

}
