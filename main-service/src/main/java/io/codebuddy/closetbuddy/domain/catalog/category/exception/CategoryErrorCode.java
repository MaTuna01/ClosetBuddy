package io.codebuddy.closetbuddy.domain.catalog.category.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CategoryErrorCode {
    // 카테고리를 찾을 수 없을 경우(404 Not Found)
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    //
    INVALID_PARENT_CATEGORY(HttpStatus.BAD_REQUEST, "유효하지 않은 상위 카테고리입니다.");
    private final HttpStatus status;
    private final String message;
}