package io.codebuddy.payservice.domain.settlement.model.vo;

public enum RawDataStatus {
    PAYMENT_COMPLETED, // 결제만 됨 (정산 대상)
    SETTLED,           // 정산 지급 완료 (재정산 방지)
    CANCELED           // 취소됨
}
