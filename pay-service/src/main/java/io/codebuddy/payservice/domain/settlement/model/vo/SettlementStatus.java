package io.codebuddy.payservice.domain.settlement.model.vo;

public enum SettlementStatus {
    SCHEDULED,    // 정산 예정
    CALCULATING,  // 정산 계산 중
    SETTLED,      // 정산 완료 (지급 완료)
    FAILED        // 정산 실패
}
