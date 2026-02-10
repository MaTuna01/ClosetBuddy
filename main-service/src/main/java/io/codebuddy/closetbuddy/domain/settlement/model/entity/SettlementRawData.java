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
    private Long productId;

    private String productName;
    private Long productPrice;
    private Integer count;
    private Long orderPrice;       // 최종 결제 금액

    // --- 상태 및 날짜 ---
    @Enumerated(EnumType.STRING)
    private RawDataStatus status;  // PAYMENT_COMPLETED, SETTLED, CANCELED

    private LocalDateTime paidAt;      // 결제일

    @Builder
    public SettlementRawData(Long paymentId, Long orderId, Long orderItemId, Long sellerId, Long memberId, Long storeId, Long productId, String productName, Long productPrice, Integer count, Long orderPrice,LocalDateTime paidAt) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.sellerId = sellerId;
        this.memberId = memberId;
        this.storeId = storeId;
        this.productId=productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.count = count;
        this.orderPrice = orderPrice;
        this.status = RawDataStatus.PAYMENT_COMPLETED; // 초기 상태: 결제 완료
        this.paidAt = paidAt;
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