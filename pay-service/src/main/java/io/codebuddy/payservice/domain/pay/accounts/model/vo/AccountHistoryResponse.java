package io.codebuddy.payservice.domain.pay.accounts.model.vo;

import java.time.LocalDateTime;

public record AccountHistoryResponse(

        Long accountAmount, //예치한 금액

        LocalDateTime createdAt,

        TransactionType type,

        Long balanceSnapshot
) {
}
