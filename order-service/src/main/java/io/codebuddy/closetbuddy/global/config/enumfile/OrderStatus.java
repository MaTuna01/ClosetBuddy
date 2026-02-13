package io.codebuddy.closetbuddy.global.config.enumfile;

public enum OrderStatus {
    CREATED,
    STOCK_CONFIRMED, //재고 차감 롤백 판단에 필요
    PAID,//예치금 차감 여부 판단, 예치금 차감 롤백 판단에 필요
    CANCELED, //주문 취소
    COMPLETED, //주문 완료
    FAILED //주문 실패
}
