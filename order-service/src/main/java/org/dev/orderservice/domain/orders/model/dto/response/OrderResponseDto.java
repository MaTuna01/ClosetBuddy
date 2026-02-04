package org.dev.orderservice.domain.orders.model.dto.response;

import java.util.List;

public record OrderResponseDto(
        Long orderId,
        List<String> productName, // 상품 이름을 저장하기 위한 상품 이름 리스트
        Long orderAmount
) {
}
