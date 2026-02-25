package io.codebuddy.closetbuddy.domain.orders.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUser;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUserInfo;
import io.codebuddy.closetbuddy.domain.orders.model.dto.request.OrderCreateRequestDto;
import io.codebuddy.closetbuddy.domain.orders.model.dto.response.OrderDetailResponseDto;
import io.codebuddy.closetbuddy.domain.orders.model.dto.response.OrderResponseDto;
import io.codebuddy.closetbuddy.domain.orders.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order API", description = "주문 관련 API")
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문을 생성합니다.
     * @param request
     * @return
     * 주문 번호를 반환해줍니다.
     */
    @Operation(
            summary = "주문 생성",
            description = "사용자의 주문 내역을 생성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "주문 생성 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 유효하지 않습니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "주문을 찾을 수 없습니다."
            )
    })
    @PostMapping
    public ResponseEntity<Long> createOrder(
            @CurrentUser CurrentUserInfo currentUser,
            @Valid @RequestBody OrderCreateRequestDto request
    ){

        Long memberId = Long.parseLong(currentUser.userId());
        Long orderId = orderService.createOrder(memberId, request);
        // 비동기 처리이므로 ACCEPTED로 상태 변경
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(orderId);
    }

    /**
     * 주문 내역을 리스트로 가져옵니다.
     * @param currentUser
     * @return
     */
    @Operation(
            summary = "주문 조회",
            description = "사용자가 주문한 목록을 조회힙니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "주문 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 주문 내역을 찾을 수 없습니다."
            )
    })
    @GetMapping("/orderList")
    public ResponseEntity<List<OrderResponseDto>> getOrder(
            @CurrentUser CurrentUserInfo currentUser
    ){
        List<OrderResponseDto> orderResponseDto = orderService.getOrder(Long.parseLong(currentUser.userId()));

        return ResponseEntity.ok(orderResponseDto);
    }


    /**
     * 주문 내역의 상세 정보를 조회합니다.
     * @param orderId
     * @return
     * OrderDetailResponseDto 를 반환합니다.
     */
    @Operation(
            summary = "주문 상세 정보 조회",
            description = "주문 내역의 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "주문 상세 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            )
    })
    @GetMapping("/{orderId}")
    public OrderDetailResponseDto getDetailOrder(
            @CurrentUser CurrentUserInfo currentUser,
            @PathVariable Long orderId
    ){
        OrderDetailResponseDto response = orderService.getDetailOrder(Long.parseLong(currentUser.userId()), orderId);

        return response;
    }


    /**
     * 주문 내역을 취소합니다.
     * @param orderId
     * @return
     */
    @Operation(
            summary = "주문 취소",
            description = "사용자의 주문 내역을 취소합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "주문 취소 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "대상을 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "CANCEL_NOT_ALLOWED"
            )
    })
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Void> canceledOrder(
            @CurrentUser CurrentUserInfo currentUser,
            @PathVariable Long orderId
    ){
        orderService.cancelOrder(Long.parseLong(currentUser.userId()), orderId);
        return ResponseEntity.ok().build();
    }

    /**
     * 장바구니에 담은 상품들을 주문합니다.
     * @param currentUser
     * @param orderId
     * @return
     */
    @PostMapping("/cart/{orderId}")
    public ResponseEntity<Long> createOrderFromCart(
            @CurrentUser CurrentUserInfo currentUser,
            @PathVariable Long orderId
    ){
        Long memberId = Long.parseLong(currentUser.userId());

        orderService.createOrderFromCart(memberId);

        return ResponseEntity.ok(orderId);
    }
}