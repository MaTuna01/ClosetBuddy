package io.codebuddy.closetbuddy.domain.catalog.sellers.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SellerUpsertRequest(
        @NotBlank(message = "판매자 이름은 필수입니다.")
        @Size(min = 1, max = 50, message = "판매자 이름은 1글자 이상 50글자 이하여야 합니다.")
        String sellerName
) {
}
