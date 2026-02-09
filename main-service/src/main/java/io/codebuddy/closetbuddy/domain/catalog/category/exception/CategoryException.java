package io.codebuddy.closetbuddy.domain.catalog.category.exception;

import lombok.Getter;

@Getter
public class CategoryException extends RuntimeException {
    private final CategoryErrorCode errorCode;

    public CategoryException(CategoryErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
