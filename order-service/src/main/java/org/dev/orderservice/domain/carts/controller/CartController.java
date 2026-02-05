package org.dev.orderservice.domain.carts.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.dev.orderservice.domain.carts.model.dto.request.CartCreateRequestDto;
import org.dev.orderservice.domain.carts.model.dto.response.CartGetResponseDto;
import org.dev.orderservice.domain.carts.service.CartService;
import org.dev.orderservice.domain.common.web.CurrentUser;
import org.dev.orderservice.domain.common.web.CurrentUserInfo;
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

    /*
     * 장바구니를 생성합니다.
     * memberId를 받지 않으면 남의 장바구니에 물건을 담아버리거나
     * 남의 장바구니를 엿볼 수 있으므로 memberId를 받음
     * @param memberPrincipalDetails
     * @param request
     * @return
     */
    @Operation(
            summary = "장바구니 생성",
            description = "사용자의 장바구니를 생성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "장바구니 생성 완료"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "중복된 장바구니 데이터"
            )
    })
    @PostMapping
    public ResponseEntity<Long> createCart(
            @CurrentUser CurrentUserInfo currentUser,
            @Valid @RequestBody CartCreateRequestDto request
    ) {
        Long cartItemId = cartService.createCart(Long.parseLong(currentUser.userId()), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartItemId);
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
                    description = "주문 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "조회할 장바구니 없음"
            )
    })
    @GetMapping
    public ResponseEntity<List<CartGetResponseDto>> getCart(
            @CurrentUser CurrentUserInfo currentUser
    ) {
        List<CartGetResponseDto> cartList = cartService.getCartList(Long.parseLong(currentUser.userId()));

        return ResponseEntity.ok(cartList);
    }


    /**
     * 장바구니 수량을 수정합니다.
     * 수량이 1개 미만이면 예외를 발생시킵니다.
     * @param currentUser
     * @param cartItemId
     * @param cartCount
     * @return
     */
    @Operation(
            summary = "장바구니 수정",
            description = "사용자의 장바구니 수량을 수정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "수량 수정 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "수정할 장바구니 상품 없음"
            )
    })
    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<Void> updateCartItem(
            @CurrentUser CurrentUserInfo currentUser,
            @PathVariable Long cartItemId,
            @RequestParam Integer cartCount
    ) {
        if(cartCount == null || cartCount < 1) {
            throw new IllegalArgumentException("수량은 최소 1개 이상이어야 합니다.");
        }

        cartService.updateCart(Long.parseLong(currentUser.userId()), cartItemId, cartCount);

        return ResponseEntity.ok().build();
    }


    /**
     * 장바구니의 물건을 삭제합니다.
     * @param currentUser
     * @param cartItemId
     * @return
     */
    @Operation(
            summary = "장바구니 물건 삭제",
            description = "장바구니 물건을 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "장바구니 물건 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "삭제할 장바구니 물건이 없음"
            )
    })
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(
            @CurrentUser CurrentUserInfo currentUser,
            @PathVariable Long cartItemId
    ) {
        cartService.deleteCartItem(Long.parseLong(currentUser.userId()), cartItemId);

        return ResponseEntity.noContent().build();
    }
}
