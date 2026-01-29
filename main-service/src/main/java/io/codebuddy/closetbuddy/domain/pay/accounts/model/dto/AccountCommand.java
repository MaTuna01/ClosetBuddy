package io.codebuddy.closetbuddy.domain.pay.accounts.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountCommand {

    @NotNull
    private Long memberId;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    @Min(1)
    private Long amount;
    // pg사 api 요구사항
    @NotBlank(message = "paymentKey는 필수 입력 값입니다.")
    @Size(max=200, message = "paymentKey는 최대 200자입니다.")
    private String paymentKey;

    @NotBlank(message = "orderId는 필수 입력 값입니다.")
    @Size(min=6, max=64, message = "orderId는 6자~64자입니다.")
    private String orderId; //결제 고유 번호
}