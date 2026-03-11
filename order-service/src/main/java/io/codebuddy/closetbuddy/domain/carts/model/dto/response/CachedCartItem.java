package io.codebuddy.closetbuddy.domain.carts.model.dto.response;

import java.io.Serializable;

// Redis 캐싱 전용 DTO
public record CachedCartItem(
        Long cartItemId,
        Long productId,
        int count
) implements Serializable {

}
