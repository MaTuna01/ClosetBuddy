package io.codebuddy.closetbuddy.domain.payments.model.entity;

import io.codebuddy.closetbuddy.domain.payments.model.vo.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "payment")
@EntityListeners(AuditingEntityListener.class) // @LastModifiedDate 사용 위함
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "payment_amount", nullable = false)
    private Long paymentAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "approved_at", updatable = false)
    private LocalDateTime approvedAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Payment(Long orderId, Long memberId, Long paymentAmount) {
        this.orderId = orderId;
        this.memberId = memberId;
        this.paymentAmount = paymentAmount;
        this.paymentStatus = PaymentStatus.PENDING; // 초기 상태
    }

    public void approved(){
        this.paymentStatus=PaymentStatus.APPROVED;
        this.approvedAt=LocalDateTime.now();
    }

    public void canceled(){
        this.paymentStatus=PaymentStatus.CANCELED;
        this.updatedAt=LocalDateTime.now();
    }

}
