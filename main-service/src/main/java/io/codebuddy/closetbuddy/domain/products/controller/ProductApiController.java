package io.codebuddy.closetbuddy.domain.products.controller;

import io.codebuddy.closetbuddy.domain.common.security.auth.MemberDetails;
import io.codebuddy.closetbuddy.domain.products.model.dto.ProductResponse;
import io.codebuddy.closetbuddy.domain.products.model.dto.UpdateProductRequest;
import io.codebuddy.closetbuddy.domain.products.model.dto.ProductCreateRequest;
import io.codebuddy.closetbuddy.domain.products.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/catalog")
public class ProductApiController {

    private final ProductService productService;

    //상품 등록
    @Operation(
            summary = "상품 등록",
            description = "판매자의 상품 정보를 등록합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "상품 등록 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "중복된 상품 데이터"
            )
    })
    @PostMapping("/stores/{storeId}/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> create(
            @AuthenticationPrincipal MemberDetails memberPrincipalDetails,
            @PathVariable Long storeId,
            @RequestBody @Valid ProductCreateRequest request
    ) {
        Long productId = productService.createProduct(memberPrincipalDetails.getId(), storeId, request);
        return ResponseEntity.ok(productId);
    }

    //상품 상세조회(단건)
    @Operation(
            summary = "상품 단건 조회",
            description = "하나의 상품을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "상품 단건 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "상품 데이터 없음"
            )
    })
    @GetMapping("products/{productId}")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable Long productId
    ) {
        ProductResponse response = productService.getProduct(productId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "상품 목록 조회",
            description = "특정 상점의 상품을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "상품 리스트 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "상품 데이터 없음(빈 리스트 조회)"
            )
    })
    //특정 상점의 상품 목록 조회
    @GetMapping("/products/{storeId}/products")
    public ResponseEntity<List<ProductResponse>> getProductsByStore(
            @PathVariable Long storeId
    ) {
        List<ProductResponse> response = productService.getProductByStoreId(storeId);
        return ResponseEntity.ok(response);
    }

    //상품 수정
    @Operation(
            summary = "상품 수정",
            description = "상품을 수정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "상품 수정 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "수정할 상품 없음"
            )
    })
    @PutMapping("products/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @AuthenticationPrincipal MemberDetails memberPrincipalDetails,
            @PathVariable Long productId,
            @RequestBody @Valid UpdateProductRequest request
    ) {
        productService.updateProduct(memberPrincipalDetails.getId(), productId, request);
        return ResponseEntity.ok().build();
    }

    //상품 삭제
    @Operation(
            summary = "상품 삭제",
            description = "상품을 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "상품 삭제 성공(반환 데이터 없음)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "삭제할 상품 없음"
            )
    })
    @DeleteMapping("products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteProduct(
            @AuthenticationPrincipal MemberDetails memberPrincipalDetails,
            @PathVariable Long productId
    ) {
        productService.deleteProduct(memberPrincipalDetails.getId(), productId);
        return ResponseEntity.noContent().build();
    }

    //전체 상품 조회
    @Operation(
            summary = "전체 상품 조회",
            description = "전체 상품을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "상품 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "상품 데이터 없음"
            )
    })
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> response = productService.getAllProducts();
        return ResponseEntity.ok(response);
    }
}
