package io.codebuddy.closetbuddy.domain.accounts.model.entity;

import io.codebuddy.closetbuddy.domain.accounts.model.vo.ChargeStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "deposit_charge") // PG 충전 내역 전용
public class DepositCharge {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "charge_id")
    private Long chargeId;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "pg_payment_key")
    private String pgPaymentKey;

    @Column(name = "pg_order_id")
    private String pgOrderId;

    @Column(name = "charge_amount")
    private Long chargeAmount;

    @Column(name = "charge_status")
    @Enumerated(EnumType.STRING)
    private ChargeStatus status;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public DepositCharge(Long chargeId, Long memberId, String pgPaymentKey, String pgOrderId, Long chargeAmount, ChargeStatus status, LocalDateTime createdAt, LocalDateTime approvedAt){
        this.chargeId=chargeId;
        this.memberId=memberId;
        this.pgPaymentKey=pgPaymentKey;
        this.pgOrderId=pgOrderId;
        this.chargeAmount=chargeAmount;
        this.status=status;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
        this.approvedAt=approvedAt;
    }

    public void cancel(){
        this.status=ChargeStatus.CANCEL;
    }

}