package io.codebuddy.closetbuddy.domain.stores.model.dto;

import java.util.List;

public record StoreListResponse(
        List<StoreResponse> stores,
        Long totalCount, //내 상점 수
        int page, //현재 페이지
        int size // 페이지 크기
) {
}
