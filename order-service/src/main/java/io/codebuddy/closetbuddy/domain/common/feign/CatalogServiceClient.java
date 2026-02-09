package io.codebuddy.closetbuddy.domain.common.feign;

import io.codebuddy.closetbuddy.domain.carts.model.dto.response.CartProductResponse;
import io.codebuddy.closetbuddy.domain.orders.model.dto.response.OrderProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "closetBuddy")
public interface CatalogServiceClient {

    // 상품 Id를 얻어오는 Feign 호출 내부 컨트롤러
    @GetMapping("/internal/catalog/products/{productId}")
    OrderProductResponse getOrderProductInfo(@PathVariable("productId") Long productId);

    // 장바구니
    @GetMapping("/internal/catalog/products/{productId}")
    CartProductResponse getCartProductInfo(@PathVariable("productId") Long productId);

}
