package io.codebuddy.closetbuddy.domain.products.model.dto;

import java.util.List;

public record ProductListResponse(
        List<ProductResponse> products,
        Long totalCount, //전체 상품 수
        int page, //현재 페이지
        int size // 페이지 크기
) {
}
