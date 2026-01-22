package io.codebuddy.closetbuddy.domain.catalog.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CatalogResult<T> {
    private String message;
    private T data;

    // 데이터 없이 메시지만 반환하는 경우 (등록, 수정, 삭제 성공 등)
    public static <T> CatalogResult<T> messageOnly(String message) {
        return new CatalogResult<>(message, null);
    }

    // 데이터와 메시지를 함께 반환하는 경우
    public static <T> CatalogResult<T> withData(String message, T data) {
        return new CatalogResult<>(message, data);
    }

    // 데이터만 반환하는 경우 (조회 등, 메시지가 필요 없다면 null)
    public static <T> CatalogResult<T> dataOnly(T data) {
        return new CatalogResult<>(null, data);
    }
}