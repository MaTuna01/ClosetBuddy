package io.codebuddy.closetbuddy.domain.stores.model.dto;

import io.codebuddy.closetbuddy.domain.sellers.model.entity.Seller;
import io.codebuddy.closetbuddy.domain.stores.model.entity.Store;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertStoreRequest(
        @NotBlank(message = "상점 이름은 필수입니다.")
        @Size(min = 1, max = 50, message = "상점 이름은 1글자 이상 50글자 이하여야 합니다.")
        String storeName
) {

    public Store toEntity(Seller seller) {
        return Store.builder()
                .storeName(this.storeName)
                .seller(seller)
                .build();
    }
}
