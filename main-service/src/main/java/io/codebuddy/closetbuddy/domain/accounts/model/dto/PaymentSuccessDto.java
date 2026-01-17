package io.codebuddy.closetbuddy.domain.accounts.model.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentSuccessDto {
    private String paymentKey; // 결제 고유 키
    private String status;     // 결제 상태 (DONE)
    private Long totalAmount;  // 결제 된 금액
    private String approvedAt; // 승인 일시
}