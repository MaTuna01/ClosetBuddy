package io.codebuddy.closetbuddy.domain.sellers.model.dto;

import jakarta.validation.constraints.NotBlank;

public record SellerUpsertRequest(
        @NotBlank(message = "판매자 이름은 필수입니다.")
        String sellerName
) {
}
