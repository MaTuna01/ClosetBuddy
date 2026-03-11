package io.codebuddy.payservice.domain.settlement.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "settlement_detail")
public class SettlementDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settle_detail_id")
    private Long settleDetailId;

    @Column(name = "settle_id", nullable = false)
    private Long settleId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "settlement_raw_data_id", nullable = false)
    private Long settlementRawDataId;

    // 어느 상점의 정산 내역인지 그룹핑 위함
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_price", nullable = false)
    private Long productPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount; // 판매 총액

    // Double은 이진수 근사치를 사용하기 때문에 다시 십진수로 변환 시 기대값과 다른 문제 발생
    // BigDecimal을 이용한 위의 정밀도 문제 해결
    @Column(name = "fee_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal feeRate; // 수수료율 스냅샷

    @Column(name = "fee_amount", nullable = false)
    private Long feeAmount; // 수수료 금액

    @Column(name = "payout_amount", nullable = false)
    private Long payoutAmount; // 정산 지급액 (총액 - 수수료)

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public SettlementDetail(Long settleId, Long memberId, Long orderId, Long orderItemId, Long productId, Long paymentId, Long settlementRawDataId,
                            String productName, Long productPrice, Integer quantity,
                            Long totalAmount, BigDecimal feeRate, Long feeAmount, Long payoutAmount, LocalDateTime createdAt,
                            Long storeId, Long sellerId) {
        this.settleId = settleId;
        this.memberId = memberId;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.paymentId = paymentId;
        this.settlementRawDataId=settlementRawDataId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.feeRate = feeRate;
        this.feeAmount = feeAmount;
        this.payoutAmount = payoutAmount;
        this.createdAt = createdAt;
        this.storeId = storeId;
        this.sellerId = sellerId;
    }

    public void setSettleId(Long settleId) {
        this.settleId = settleId;
    }
}



