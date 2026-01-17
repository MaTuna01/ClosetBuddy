package io.codebuddy.closetbuddy.domain.orders.dto.response;

import java.util.List;

public record OrderResponseDto(
        Long orderId,
        List<String> productName,
        Long orderPrice
) {
}
