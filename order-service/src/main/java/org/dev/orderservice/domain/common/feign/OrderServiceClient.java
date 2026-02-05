package org.dev.orderservice.domain.common.feign;

import org.dev.orderservice.domain.carts.model.dto.response.CartProductResponse;
import org.dev.orderservice.domain.orders.model.dto.response.OrderProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "main-service", url = "http://localhost:8082")
public interface OrderServiceClient {

    // 주문
    @GetMapping("/internal/catalog/products/{productId}")
    OrderProductResponse getProductWithOrder(@PathVariable("productId") Long productId);

    // 장바구니
    @GetMapping("/internal/catalog/products/{productId}")
    CartProductResponse getProductWithCart(@PathVariable("productId") Long productId);
}