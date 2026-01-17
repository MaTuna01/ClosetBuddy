package io.codebuddy.closetbuddy.domain.accounts.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record AccountChargeResponse(
        @NotNull
        Long accountAmount, //예치된 금액

        @NotNull
        @Positive
        Long balanceSnapshot, //변동 후 잔액

        @NotBlank
        LocalDateTime createdAt,

        @NotBlank
        TransactionType type
) {
}
