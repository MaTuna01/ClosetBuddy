package io.codebuddy.closetbuddy.domain.carts;

import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "main-service")
public interface ProductClient {
    // 단건 조회 목록
    @GetMapping("/api/v1/catalog/products/{productId}")
    ProductResponse getProduct(@PathVariable("productId") Long productId);
}
