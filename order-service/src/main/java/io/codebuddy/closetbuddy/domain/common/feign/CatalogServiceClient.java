package io.codebuddy.closetbuddy.domain.common.feign;

import io.codebuddy.closetbuddy.domain.common.dto.RecommendProductInfoResponse;
import io.codebuddy.closetbuddy.domain.common.feign.dto.CartProductResponse;
import io.codebuddy.closetbuddy.domain.common.feign.dto.OrderProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "closetBuddy")
public interface CatalogServiceClient {

    // 상품 Id를 얻어오는 Feign 호출 내부 컨트롤러
    @GetMapping("/internal/catalog/products/{productId}")
    OrderProductResponse getOrderProductInfo(@PathVariable("productId") Long productId);

    // 장바구니
    @GetMapping("/internal/catalog/products/{productId}")
    CartProductResponse getCartProductInfo(@PathVariable("productId") Long productId);

    // 상품 추천 시스템에서 사용하는 컨트롤러
    @GetMapping("/internal/catalog/products")
    List<RecommendProductInfoResponse> getRecommendProductInfo(@RequestParam("productIds") List<Long> productIds);

}
