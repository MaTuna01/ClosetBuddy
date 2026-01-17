package io.codebuddy.closetbuddy.domain.stores.model.dto;

import io.codebuddy.closetbuddy.domain.sellers.model.entity.Seller;
import io.codebuddy.closetbuddy.domain.stores.model.entity.Store;
import jakarta.validation.constraints.NotBlank;

public record UpsertStoreRequest(
        @NotBlank(message = "상점 이름은 필수입니다.")
        String storeName
) {

    public Store toEntity(Seller seller) {
        return Store.builder()
                .storeName(this.storeName)
                .seller(seller)
                .build();
    }
}
