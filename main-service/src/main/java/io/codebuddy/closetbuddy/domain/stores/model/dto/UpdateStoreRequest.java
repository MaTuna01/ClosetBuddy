package io.codebuddy.closetbuddy.domain.stores.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateStoreRequest(
        @NotBlank
        @Size(min = 1, max = 50, message = "가게 이름은 1글자 이상 50글자 이하이어야 합니다.")
        String storeName
        //추후 가게 설명, 이미지 등 추가 가능
) {
}
