package io.codebuddy.closetbuddy.domain.carts.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartCreateRequestDto(
        @NotNull
        Long productId,
        @Min(1)
        Integer cartCount
){

}
