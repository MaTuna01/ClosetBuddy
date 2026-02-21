package io.codebuddy.closetbuddy.domain.catalog.products.controller;

import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.InternalProductResponse;
import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.InternalRecommendProductResponse;
import io.codebuddy.closetbuddy.domain.catalog.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/catalog")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;

    @GetMapping("/products/{productId}")
    public ResponseEntity<InternalProductResponse>getProductInfo(@PathVariable Long productId) {
        InternalProductResponse product = productService.getInternalProduct(productId);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/products")
    public ResponseEntity<List<InternalRecommendProductResponse>>getProductInfos(
            @RequestParam("productIds") List<Long> productIds
    ) {
        List<InternalRecommendProductResponse> products = productService.getInternalProducts(productIds);
        return ResponseEntity.ok(products);
    }
}
