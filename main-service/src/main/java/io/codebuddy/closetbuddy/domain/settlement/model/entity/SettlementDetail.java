package io.codebuddy.closetbuddy.domain.settlement.model.entity;


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

    // [원천 데이터 논리적 참조]
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    // [스냅샷 데이터]
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_price", nullable = false)
    private Long productPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // [금액 계산]
    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "fee_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal feeRate; // 수수료율 스냅샷

    @Column(name = "fee_amount", nullable = false)
    private Long feeAmount;

    @Column(name = "payout_amount", nullable = false)
    private Long payoutAmount;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public SettlementDetail(Long settleId, Long orderId, Long orderItemId, Long productId, Long paymentId,
                            String productName, Long productPrice, Integer quantity,
                            Long totalAmount, BigDecimal feeRate, Long feeAmount, Long payoutAmount) {
        this.settleId = settleId;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.paymentId = paymentId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.feeRate = feeRate;
        this.feeAmount = feeAmount;
        this.payoutAmount = payoutAmount;
    }
}


