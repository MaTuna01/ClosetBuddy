package io.codebuddy.closetbuddy.domain.accounts.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountCommand {

    @NotBlank
    private Long memberId;

    @NotBlank(message = "가격은 필수 입력 값입니다.")
    @Min(1)
    private Long amount;
    // pg사 api 요구사항
    @NotBlank(message = "paymentKey는 필수 입력 값입니다.")
    @Max(value = 200, message = "paymentKey는 최대 200자입니다.")
    private String paymentKey;

    @NotBlank(message = "orderId는 필수 입력 값입니다.")
    @Min(value = 6, message = "orderId는 최소 6자입니다.")
    @Max(value = 64, message = "orderId는 최대 64자입니다.")
    private String orderId; //결제 고유 번호
}