package io.codebuddy.closetbuddy.domain.accounts.model.vo;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AccountHistoryResponse(
        @NotNull
        Long accountAmount, //예치한 금액

        @NotNull
        LocalDateTime createdAt,

        @NotNull
        TransactionType type,

        @NotNull
        Long balanceSnapshot
) {
}
