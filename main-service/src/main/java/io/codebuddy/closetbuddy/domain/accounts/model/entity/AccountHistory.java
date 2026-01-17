package io.codebuddy.closetbuddy.domain.accounts.model.entity;

import io.codebuddy.closetbuddy.domain.accounts.model.vo.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "account_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_history_id")
    private Long accountHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;


    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(name = "account_amount")
    private Long amount;

    @Column(name = "balance_snapshot")
    private Long balanceSnapshot; //변동 후 잔액

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Column(name = "ref_id")
    private Long refId;           // paymentKey 대신 논리적 ID 저장
    // CHARGE면 deposit_charge.id
    // USE면 payment.id


    @Builder
    public AccountHistory(Long accountHistoryId, Account account, TransactionType type, Long amount, Long balanceSnapshot, LocalDateTime createdAt, Long refId) {
        this.accountHistoryId = accountHistoryId;
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.balanceSnapshot=balanceSnapshot;
        this.createdAt = createdAt;
        this.refId = refId;
    }

}


