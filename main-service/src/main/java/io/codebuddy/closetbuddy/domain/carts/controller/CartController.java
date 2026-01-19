package io.codebuddy.closetbuddy.domain.carts.controller;

import io.codebuddy.closetbuddy.domain.carts.dto.request.CartCreateRequestDto;
import io.codebuddy.closetbuddy.domain.carts.dto.response.CartGetResponseDto;
import io.codebuddy.closetbuddy.domain.carts.service.CartService;
import io.codebuddy.closetbuddy.domain.common.security.auth.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * 남의 장바구니를 엿볼 수 있으므로 memberId를 받는다.
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
            @AuthenticationPrincipal MemberDetails memberPrincipalDetails,
            @Valid @RequestBody CartCreateRequestDto request
    ) {
        Long cartItemId = cartService.createCart(memberPrincipalDetails.getMember().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartItemId);
    }

    /**
     * 장바구니를 조회합니다.
     * 회원 아이디를 통해 조회합니다.
     * @param memberPrincipalDetails
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
            @AuthenticationPrincipal MemberDetails memberPrincipalDetails
    ) {
        Long memberId = memberPrincipalDetails.getMember().getId();
        List<CartGetResponseDto> cartList = cartService.getCartList(memberId);

        return ResponseEntity.ok(cartList);
    }


    /**
     * memberId를 통해 장바구니 수량을 수정합니다.
     * 수량이 1개 미만으로 떨어지면 예외를 발생시킵니다.
     * @param memberPrincipal
     * @param cartItemId
     * @param cartCount
     * @return
     */
    @Operation(
            summary = "장바구니 조회",
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
            @AuthenticationPrincipal MemberDetails memberPrincipal,
            @PathVariable Long cartItemId,
            @RequestParam Integer cartCount
    ) {
        if(cartCount == null || cartCount < 1) {
            throw new IllegalArgumentException("수량은 최소 1개 이상이어야 합니다.");
        }

        Long memberId = memberPrincipal.getMember().getId();

        cartService.updateCart(memberId, cartItemId, cartCount);

        return ResponseEntity.ok().build();
    }


    /**
     * 장바구니의 물건을 삭제합니다.
     * @param memberPrincipal
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
            @AuthenticationPrincipal MemberDetails memberPrincipal,
            @PathVariable Long cartItemId
    ) {
        Long memberId = memberPrincipal.getMember().getId();

        cartService.deleteCartItem(memberPrincipal.getMember().getId(), cartItemId);

        return ResponseEntity.noContent().build();
    }

}
