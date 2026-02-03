package io.codebuddy.closetbuddy.domain.pay.accounts.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "account")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "balance", nullable = false)
    private Long balance=0L;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Builder
    public Account(Long memberId, Long balance){
        this.memberId=memberId;
        this.balance = (balance != null) ? balance : 0L;
    }
    public static Account createAccount(Long memberId) {
        return Account.builder()
                .memberId(memberId)
                .balance(0L)
                .build();
    }

    public void charge(Long amount){
        if(amount<=0) throw new IllegalArgumentException("충전 금액은 0보다 커야합니다.");
        this.balance+=amount;
    }

    public void withdraw(Long amount){
        if(this.balance<amount) throw new IllegalArgumentException("잔액이 부족합니다.");
        this.balance-=amount;
    }

}
