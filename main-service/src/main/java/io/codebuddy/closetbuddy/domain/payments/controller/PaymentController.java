package io.codebuddy.closetbuddy.domain.payments.controller;

import io.codebuddy.closetbuddy.domain.common.security.auth.MemberDetails;
import io.codebuddy.closetbuddy.domain.payments.model.vo.PaymentRequest;
import io.codebuddy.closetbuddy.domain.payments.model.vo.PaymentResponse;
import io.codebuddy.closetbuddy.domain.payments.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            description = "사용자의 결제 내역을 생성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "결제 생성 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "중복된 결제 데이터"
            )
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> payOrder(
            @AuthenticationPrincipal MemberDetails principal,
            @RequestBody PaymentRequest request
    ) {
        Long memberId = principal.getMember().getId();
        return ResponseEntity.ok(paymentService.payOrder(memberId, request));
    }

    // 결제 취소
    @Operation(
            summary = "결제 취소",
            description = "사용자의 결제를 취소합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "결제 취소 성공"
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
    @PostMapping("/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @AuthenticationPrincipal MemberDetails principal,
            @RequestBody PaymentRequest request
    ) {
        Long memberId = principal.getMember().getId();
        return ResponseEntity.ok(paymentService.payCancel(memberId, request));
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
                    description = "결제 내역을 찾을 수 없음"
            )
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentDetail(
            @AuthenticationPrincipal MemberDetails principal,
            @PathVariable Long orderId
    ) {
        Long memberId = principal.getMember().getId();
        return ResponseEntity.ok(paymentService.getPayment(memberId, orderId));
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
            @AuthenticationPrincipal MemberDetails principal
    ) {
        return ResponseEntity.ok(paymentService.getPayments(principal.getMember().getId()));
    }

}
