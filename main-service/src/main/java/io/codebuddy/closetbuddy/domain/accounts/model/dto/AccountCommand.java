package io.codebuddy.closetbuddy.domain.accounts.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountCommand {
    private Long memberId;
    private Long amount;
    // pg사 api 요구사항
    private String paymentKey;
    private String orderId; //결제 고유 번호
}