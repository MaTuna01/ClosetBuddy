package io.codebuddy.closetbuddy.event;

// 재고 요청 단위
public record StockItem(
        Long productId,
        int quantity    // 요청 수량
) {
}
