package io.codebuddy.closetbuddy.domain.carts.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", contextId = "CartProductClient", url = "http://localhost:8082")
public interface CartProductClient {
    // 단건 조회 목록
    @GetMapping("/api/v1/catalog/products/{productId}")
    CartProductResponse getProduct(@PathVariable("productId") Long productId);
}