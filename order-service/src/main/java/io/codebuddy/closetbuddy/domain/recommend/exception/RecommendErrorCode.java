package io.codebuddy.closetbuddy.domain.recommend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum RecommendErrorCode {

    // 장바구니를 찾을 수 없을 경우(404 Not Found)
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 안에 상품이 없습니다."),
    RECOMMEND_NOT_FOUND(HttpStatus.NOT_FOUND, "추천 상품을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    RecommendErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
