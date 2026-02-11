package io.codebuddy.payservice.domain.pay.payments.controller;


import io.codebuddy.payservice.domain.common.web.CurrentUser;
import io.codebuddy.payservice.domain.common.web.CurrentUserInfo;
import io.codebuddy.payservice.domain.pay.payments.model.vo.PaymentRequest;
import io.codebuddy.payservice.domain.pay.payments.model.vo.PaymentResponse;
import io.codebuddy.payservice.domain.pay.payments.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 수행
    @Operation(
            summary = "결제 생성",
            description = "사용자의 주문 요청에 대한 결제를 수행합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "결제 수행 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 결제 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "잘못된 결제 정보"
            )
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> payOrder(
            @CurrentUser CurrentUserInfo currentUser,
            @RequestBody @Valid PaymentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.payOrder(Long.parseLong(currentUser.userId()), request));
    }

    // 특정 결제 내역 취소
    @Operation(
            summary = "특정 결제 내역 취소",
            description = "사용자의 결제를 취소합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "결제 취소 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "결제 정보를 찾을 수 없음"
            )
    })
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @CurrentUser CurrentUserInfo currentUser,
            @PathVariable Long paymentId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.payCancel(Long.parseLong(currentUser.userId()), paymentId));
    }

    // 결제 단건 조회
    @Operation(
            summary = "결제 단건 조회",
            description = "하나의 결제 내역을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "결제 단건 내역 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "결제 내역을 찾을 수 없거나 접근 권한이 없음"
            )
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentDetail(
            @CurrentUser CurrentUserInfo currentUser,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(paymentService.getPayment(Long.parseLong(currentUser.userId()), orderId));
    }

    // 결제 내역 전체 조회
    @Operation(
            summary = "전체 결제 내역 조회",
            description = "사용자의 전체 결제 내역을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "결제 내역 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "결제 내역을 찾을 수 없음"
            )
    })
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPaymentList(
            @CurrentUser CurrentUserInfo currentUser
    ) {
        return ResponseEntity.ok(paymentService.getPayments(Long.parseLong(currentUser.userId())));
    }

}
