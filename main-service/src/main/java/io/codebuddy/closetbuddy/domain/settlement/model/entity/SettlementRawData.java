package io.codebuddy.closetbuddy.domain.settlement.model.entity;

import io.codebuddy.closetbuddy.domain.settlement.model.vo.RawDataStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "settlement_raw_data", indexes = {
        @Index(name = "idx_settle_target", columnList = "status, confirmed_at") // 배치 성능용 인덱스
})
public class SettlementRawData {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementRawDataId;

    // --- 식별자 정보 ---
    private Long paymentId;
    private Long orderId;
    private Long orderItemId;

    // --- 스냅샷 데이터  ---
    private Long sellerId;
    private Long memberId;
    private Long storeId;

    private String productName;
    private Long productPrice;
    private Integer count;
    private Long orderPrice;       // 최종 결제 금액

    private Double feeRate;        // 결제 시점의 수수료율

    // --- 상태 및 날짜 ---
    @Enumerated(EnumType.STRING)
    private RawDataStatus status;  // PAYMENT_COMPLETED, ORDER_CONFIRMED, SETTLED, CANCELED

    private LocalDateTime paidAt;      // 결제일
    private LocalDateTime confirmedAt; // 구매 확정일 (정산 배치의 기준 날짜)

    @Builder
    public SettlementRawData(Long paymentId, Long orderId, Long orderItemId, Long sellerId, Long memberId, Long storeId, String productName, Long productPrice, Integer count, Long orderPrice, Double feeRate) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.sellerId = sellerId;
        this.memberId = memberId;
        this.storeId = storeId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.count = count;
        this.orderPrice = orderPrice;
        this.feeRate = feeRate;
        this.status = RawDataStatus.PAYMENT_COMPLETED; // 초기 상태: 결제 완료
        this.paidAt = LocalDateTime.now();
    }

    // 주문 확정 (3일 후 호출됨)
    public void confirmOrder() {
        this.status = RawDataStatus.ORDER_CONFIRMED;
        this.confirmedAt = LocalDateTime.now(); // ★ 이때 날짜가 찍힘
    }

    // 정산 완료 (배치 수행 후)
    public void completeSettlement() {
        this.status = RawDataStatus.SETTLED;
    }

    // 결제 취소
    public void cancel() {
        this.status = RawDataStatus.CANCELED;
    }
}