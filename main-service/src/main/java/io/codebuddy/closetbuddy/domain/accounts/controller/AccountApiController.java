package io.codebuddy.closetbuddy.domain.accounts.controller;

import io.codebuddy.closetbuddy.domain.accounts.model.dto.AccountCommand;
import io.codebuddy.closetbuddy.domain.accounts.model.vo.*;
import io.codebuddy.closetbuddy.domain.accounts.service.AccountService;
import io.codebuddy.closetbuddy.domain.common.security.auth.MemberDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountApiController {

    private final AccountService accountService;

    //예치금 조회
    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getAccountBalance(
            @AuthenticationPrincipal MemberDetails principal
    ){
        // principal에서 Member 객체를 꺼내고 -> ID를 가져옴
        Long memberId = principal.getMember().getId();
        AccountResponse accountResponse = accountService.getAccountBalance(memberId);
        return ResponseEntity.ok(accountResponse);
    }

    //예치금 등록
    @PostMapping("/charge")
    public ResponseEntity<AccountHistoryResponse> chargeAccount(
            @AuthenticationPrincipal MemberDetails principal,
            @RequestBody PaymentConfirmRequest request
    ) {

        Long memberId = principal.getMember().getId();

        AccountCommand command = new AccountCommand(
                memberId,
                request.amount(),
                request.orderId(),
                request.paymentKey()
        );
        AccountHistoryResponse response = accountService.charge(command);

        return ResponseEntity.ok(response);
    }

    // 예치 내역 전체 조회
    @GetMapping("/history")
    public ResponseEntity<List<AccountHistoryResponse>> getAccountHistory(
            @AuthenticationPrincipal MemberDetails principal
    ) {
        Long memberId = principal.getMember().getId();

        List<AccountHistoryResponse> historyList = accountService.getHistoryAll(memberId);

        return ResponseEntity.ok(historyList);
    }

    // 예치금 내역 상세(단건) 조회
    @GetMapping("/history/{accountHistoryId}")
    public ResponseEntity<AccountHistoryResponse> getAccountHistoryDetail(
            @AuthenticationPrincipal MemberDetails principal,
            @PathVariable Long accountHistoryId
    ) {

        Long memberId = principal.getMember().getId();

        AccountHistoryResponse response = accountService.getHistory(memberId, accountHistoryId);

        return ResponseEntity.ok(response);
    }

    // 예치 취소 (환불)
    /*
    DeleteMapping을 사용할 경우 일부 환경에서 body 무시 위험
    + 토스 api 사용 및 환불 사유 기록 로직을 수행하기 때문에 PostMapping 사용
     */
    @PostMapping("/history/{accountHistoryId}/cancel")
    public ResponseEntity<AccountHistoryResponse> cancelHistory(
            @AuthenticationPrincipal MemberDetails principal,
            @PathVariable Long accountHistoryId,
            @RequestBody @Valid TossCancelRequest request
    ) {

        Long memberId = principal.getMember().getId();

        AccountHistoryResponse response=accountService.deleteHistory(memberId, accountHistoryId, request.cancelReason());

        return ResponseEntity.ok(response);
    }


}
