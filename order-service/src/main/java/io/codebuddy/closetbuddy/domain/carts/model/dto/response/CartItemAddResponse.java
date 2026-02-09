package io.codebuddy.closetbuddy.domain.carts.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartItemAddResponse<T> {
    private String message;
    private T data;

    // 데이터 없이 메시지만 반환하는 경우 (등록, 수정, 삭제 성공 등)
    public static <T> CartItemAddResponse<T> messageOnly(String message) {
        return new CartItemAddResponse<>(message, null);
    }

    // 데이터와 메시지를 함께 반환하는 경우
    public static <T> CartItemAddResponse<T> withData(String message, T data) {
        return new CartItemAddResponse<>(message, data);
    }

    // 데이터만 반환하는 경우 (조회 등, 메시지가 필요 없다면 null)
    public static <T> CartItemAddResponse<T> dataOnly(T data) {
        return new CartItemAddResponse<>(null, data);
    }
}
