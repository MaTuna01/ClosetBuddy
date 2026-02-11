package io.codebuddy.closetbuddy.domain.catalog.products.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StockErrorCode {

    LOCK_ACQUISITION_FAILURE(423, "재고 처리 중입니다. 잠시 후 다시 시도해주세요."),
    INSUFFICIENT_STOCK(409, "재고가 부족합니다."),
    LOCK_INTERRUPTED(503, "서버 처리 중 문제가 발생했습니다."),
    PRODUCT_NOT_FOUND(404, "상품을 찾을 수 없습니다.");

    private final int status;
    private final String message;
}