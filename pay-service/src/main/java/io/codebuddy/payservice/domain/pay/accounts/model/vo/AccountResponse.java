package io.codebuddy.payservice.domain.pay.accounts.model.vo;

public record AccountResponse(

        Long memberId,

        Long balance,

        String message
) {
}
