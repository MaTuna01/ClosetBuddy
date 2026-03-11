package io.codebuddy.payservice.domain.pay.payments.controller;


import io.codebuddy.payservice.domain.common.web.CurrentUser;
import io.codebuddy.payservice.domain.common.web.CurrentUserInfo;
import io.codebuddy.payservice.domain.pay.payments.model.vo.PaymentResponse;
import io.codebuddy.payservice.domain.pay.payments.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

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
