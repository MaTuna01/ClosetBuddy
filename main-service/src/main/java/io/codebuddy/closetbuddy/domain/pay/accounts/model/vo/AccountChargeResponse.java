package io.codebuddy.closetbuddy.domain.pay.accounts.model.vo;

import java.time.LocalDateTime;

public record AccountChargeResponse(

        Long accountAmount, //예치된 금액

        Long balanceSnapshot, //변동 후 잔액

        LocalDateTime createdAt,

        TransactionType type
) {
}
