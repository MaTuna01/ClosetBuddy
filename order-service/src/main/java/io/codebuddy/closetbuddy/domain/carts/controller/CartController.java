package io.codebuddy.closetbuddy.domain.carts.controller;


import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartDeleteRequest;
import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartItemAddRequest;
import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartUpdateRequest;
import io.codebuddy.closetbuddy.domain.common.web.dto.CartResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.codebuddy.closetbuddy.domain.carts.model.dto.response.CartGetResponseDto;
import io.codebuddy.closetbuddy.domain.carts.service.CartService;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUser;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUserInfo;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * 장바구니에 상품을 추가합니다.
     * @param currentUser
     * @param request
     * @return
     */
    @Operation(
            summary = "장바구니 상품 추가",
            description = "사용자의 장바구니에 상품을 추가합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "장바구니 상품 추가 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 상품을 찾을 수 없습니다."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 유효하지 않습니다."
            )
    })
    @PostMapping("/items")
    public ResponseEntity<CartResult<Long>> addItemToCart(
            @CurrentUser CurrentUserInfo currentUser,
            @Valid @RequestBody CartItemAddRequest request
    ) {
        // 유효성 검사
        if (request.productId() == null || request.productId() <= 0) {
            throw new IllegalArgumentException("유효하지 않은 상품 ID 입니다.");
        }
        if (request.productCount() == null || request.productCount() < 1) {
            throw new IllegalArgumentException("수량은 최소 1개 이상이어야 합니다.");
        }

        Long cartItemId = cartService.addCartItemToCart(
                request,
                Long.parseLong(currentUser.userId())
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CartResult.success("장바구니에 상품 추가 성공", cartItemId));
    }

    /**
     * 회원 아이디를 통해 장바구니를 조회합니다.
     * @param currentUser
     * @return
     */
    @Operation(
            summary = "장바구니 조회",
            description = "사용자의 장바구니 내역을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "장바구니 리스트 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "장바구니가 비어있습니다"
            )
    })
    @GetMapping
    public ResponseEntity<CartResult<List<CartGetResponseDto>>> getCart(
            @CurrentUser CurrentUserInfo currentUser
    ) {
        List<CartGetResponseDto> cartList = cartService.getCartList(Long.parseLong(currentUser.userId()));

        if(cartList == null || cartList.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(CartResult.success("장바구니가 비어있습니다.", cartList));
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CartResult.success("장바구니 리스트 조회 성공", cartList));
    }


    /**
     * 장바구니를 수정합니다.
     * @param currentUser
     * @param request
     * @return
     */
    @Operation(
            summary = "장바구니 수정",
            description = "사용자의 장바구니 수량을 수정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "상품 수량 수정 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 유효하지 않습니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "장바구니 안에 상품이 없습니다."
            )
    })
    @PatchMapping("/items")
    public ResponseEntity<CartResult<Void>> updateCartItem(
            @CurrentUser CurrentUserInfo currentUser,
            @Valid @RequestBody CartUpdateRequest request
    ) {
        cartService.updateCart(Long.parseLong(currentUser.userId()), request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CartResult.success("상품 수량 수정 성공"));
    }

    /**
     * 장바구니에 있는 상품 목록을 삭제합니다.
     * @param currentUser
     * @param request
     * @return
     */
    @Operation(
            summary = "장바구니 물건 삭제",
            description = "장바구니 물건을 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "장바구니 상품 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 유효하지 않습니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "삭제할 장바구니 물건이 없음"
            )
    })
    @DeleteMapping("/items")
    public ResponseEntity<CartResult<Void>> deleteCartItem(
            @CurrentUser CurrentUserInfo currentUser,
            @Valid @RequestBody CartDeleteRequest request
    ) {
        cartService.deleteCartItem(Long.parseLong(currentUser.userId()), request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CartResult.success("장바구니 상품 삭제 성공"));
    }

}
