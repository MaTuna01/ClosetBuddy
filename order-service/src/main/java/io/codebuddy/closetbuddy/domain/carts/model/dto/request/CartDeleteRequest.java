package io.codebuddy.closetbuddy.domain.carts.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// cartItemId를 삭제하기 위한 Dto
public record CartDeleteRequest(
        @NotEmpty(message = "삭제할 상품 목록은 필수입니다.")
        List<Long> cartItemList
) {
}
