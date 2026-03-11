package io.codebuddy.closetbuddy.event;

import java.util.List;

// 재고 확인 요청 DTO
public record StockCheckRequest(
        Long orderId,
        Long memberId,
        List<StockItem> items
) {
}
