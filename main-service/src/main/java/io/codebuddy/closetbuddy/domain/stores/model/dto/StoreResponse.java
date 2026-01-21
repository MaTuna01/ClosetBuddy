package io.codebuddy.closetbuddy.domain.stores.model.dto;

import io.codebuddy.closetbuddy.domain.stores.model.entity.Store;

public record StoreResponse(
        Long storeId,
        String SellerName,
        String storeName
) {

    //Store 엔티티를 DTO로 변환해주는 메서드
    public static StoreResponse from(Store store) {
        return new StoreResponse(
                store.getId(),
                store.getSeller().getSellerName(),
                store.getStoreName()
        );
    }
}
