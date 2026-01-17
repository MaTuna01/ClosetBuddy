package io.codebuddy.closetbuddy.domain.orders.controller;

import io.codebuddy.closetbuddy.domain.common.security.auth.MemberDetails;
import io.codebuddy.closetbuddy.domain.orders.dto.request.OrderRequestDto;
import io.codebuddy.closetbuddy.domain.orders.dto.response.OrderDetailResponseDto;
import io.codebuddy.closetbuddy.domain.orders.dto.response.OrderResponseDto;
import io.codebuddy.closetbuddy.domain.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문을 생성합니다.
     * @param request
     * @return
     */
    @Operation(
            summary = "주문 생성",
            description = "사용자의 주문 내역을 생성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "주문 생성 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "중복된 주문 데이터"
            )
    })
    @PostMapping
    public ResponseEntity<OrderRequestDto> createOrder(
            @RequestBody OrderRequestDto request
    ){
        return ResponseEntity.ok(request);
    }

    /**
     * 주문 내역을 리스트로 가져옵니다.
     * @param memberPrincipal
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
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "주문 내역을 찾을 수 없음"
            )
    })
    @GetMapping("/orderList")
    public ResponseEntity<List<OrderResponseDto>> getOrder(
            @AuthenticationPrincipal MemberDetails memberPrincipal
    ){
        Long memberId = memberPrincipal.getMember().getId();
        List<OrderResponseDto> orderResponseDto = orderService.getOrder(memberId);

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
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "중복된 상품 데이터"
            )
    })
    @GetMapping("/{orderId}")
    public OrderDetailResponseDto getDetailOrder(
            @PathVariable Long orderId
    ){
        OrderDetailResponseDto response = orderService.getDetailOrder(orderId);

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
                    responseCode = "204",
                    description = "주문 취소 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "대상을 찾을 수 없음"
            )
    })
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Void> canceledOrder(
            @PathVariable Long orderId,
            @PathVariable Long memberId
    ){
        orderService.cancelledOrder(memberId, orderId);
        return ResponseEntity.ok().build();
    }
}
