package io.codebuddy.closetbuddy.domain.accounts.model.vo;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AccountHistoryResponse(

        Long accountAmount, //예치한 금액

        LocalDateTime createdAt,

        TransactionType type,

        Long balanceSnapshot
) {
}
