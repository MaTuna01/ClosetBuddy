package io.codebuddy.closetbuddy.domain.accounts.model.vo;

import jakarta.validation.constraints.NotNull;

public record AccountResponse(

        Long memberId,

        Long balance,

        String message
) {
}
