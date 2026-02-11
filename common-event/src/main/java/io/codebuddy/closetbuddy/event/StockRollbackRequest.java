package io.codebuddy.closetbuddy.event;

import java.util.List;

// 재고 복구 요청 DTO
public record StockRollbackRequest(
        Long orderId,
        List<StockItem> items
) {
}
