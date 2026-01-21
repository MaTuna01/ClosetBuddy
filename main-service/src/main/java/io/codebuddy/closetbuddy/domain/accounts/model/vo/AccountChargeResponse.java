package io.codebuddy.closetbuddy.domain.accounts.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record AccountChargeResponse(

        Long accountAmount, //예치된 금액

        Long balanceSnapshot, //변동 후 잔액

        LocalDateTime createdAt,

        TransactionType type
) {
}
