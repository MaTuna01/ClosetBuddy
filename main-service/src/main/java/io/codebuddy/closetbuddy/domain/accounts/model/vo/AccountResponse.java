package io.codebuddy.closetbuddy.domain.accounts.model.vo;

import jakarta.validation.constraints.NotNull;

public record AccountResponse(
        @NotNull
        Long memberId,

        @NotNull
        Long balance,

        String message
) {
}
