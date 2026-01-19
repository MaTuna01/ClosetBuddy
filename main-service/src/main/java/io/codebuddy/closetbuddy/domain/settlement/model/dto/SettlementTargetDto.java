package io.codebuddy.closetbuddy.domain.settlement.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SettlementTargetDto {

    // 원본 데이터 추적용 ID
    private Long orderId;
    private Long orderItemId;
    private Long productId;
    private Long paymentId;
    private Long sellerId;
    private Long storeId;

    // 스냅샷을 찍기 위한 원본 정보
    private String productName;
    private Long price;    // 판매 당시 단가
    private Integer count; // 구매 수량

    // 생성자 (QueryDSL 사용 위함)
    public SettlementTargetDto(Long orderId, Long orderItemId, Long productId, Long paymentId,
                               Long sellerId, Long storeId,
                               String productName, Long price, Integer count) {
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.paymentId = paymentId;
        this.sellerId = sellerId;
        this.storeId = storeId;
        this.productName = productName;
        this.price = price;
        this.count = count;
    }
}
