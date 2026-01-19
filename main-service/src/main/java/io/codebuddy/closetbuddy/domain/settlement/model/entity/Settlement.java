package io.codebuddy.closetbuddy.domain.settlement.model.entity;


import io.codebuddy.closetbuddy.domain.settlement.model.vo.SettlementStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "settlement")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settle_id")
    private Long settleId;

    // [관계 정보]
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    // [집계 정보]
    @Column(name = "total_sales_amount", nullable = false)
    private Long totalSalesAmount; // 총 매출액

    @Column(name = "payout_amount", nullable = false)
    private Long payoutAmount; // 최종 지급액

    // [상태 및 기준]
    @Enumerated(EnumType.STRING)
    @Column(name = "settle_status", nullable = false)
    private SettlementStatus settleStatus;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate; //LocalDate : 날짜 정보만

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    public Settlement(Long storeId, Long sellerId, Long totalSalesAmount, Long payoutAmount, SettlementStatus settleStatus, LocalDate settlementDate, LocalDateTime createdAt) {
        this.storeId = storeId;
        this.sellerId = sellerId;
        this.totalSalesAmount = totalSalesAmount;
        this.payoutAmount = payoutAmount;
        this.settleStatus = settleStatus;
        this.settlementDate = settlementDate;
        this.createdAt= LocalDateTime.now();
    }

    // 매출액 누적
    public void addAmounts(Long salesAmount, Long payoutAmount) {
        this.totalSalesAmount += salesAmount;
        this.payoutAmount += payoutAmount;
    }

}
