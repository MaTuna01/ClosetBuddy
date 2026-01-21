package io.codebuddy.closetbuddy.domain.accounts.controller;

import io.codebuddy.closetbuddy.domain.accounts.model.dto.AccountCommand;
import io.codebuddy.closetbuddy.domain.accounts.model.vo.*;
import io.codebuddy.closetbuddy.domain.accounts.service.AccountService;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUser;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Account", description = "예치금 조회, 충전 및 결제 내역 관리 API")
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountApiController {

    private final AccountService accountService;

    //예치금 조회
    @Operation(summary = "예치금 잔액 조회", description = "현재 로그인한 사용자의 예치금 잔액 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getAccountBalance(
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo currentUser
    ){
        AccountResponse accountResponse = accountService.getAccountBalance(Long.parseLong(currentUser.userId()));
        return ResponseEntity.ok(accountResponse);
    }



    //예치금 등록
    @Operation(summary = "예치금 충전(결제 승인)", description = "토스페이먼츠 결제 성공 후 전달받은 정보를 바탕으로 예치금을 충전합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "충전 완료",
                    content = @Content(schema = @Schema(implementation = AccountHistoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "결제 정보 불일치 또는 유효하지 않은 요청", content = @Content)
    })
    @PostMapping("/charge")
    public ResponseEntity<AccountHistoryResponse> chargeAccount(
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo currentUser,
            @RequestBody @Valid PaymentConfirmRequest request
    ) {

        AccountCommand command = new AccountCommand(
                Long.parseLong(currentUser.userId()),
                request.amount(),
                request.paymentKey(),
                request.orderId()
        );
        AccountHistoryResponse response = accountService.charge(command);

        return ResponseEntity.ok(response);
    }




    // 예치 내역 전체 조회
    @Operation(summary = "예치금 내역 전체 조회", description = "현재 사용자의 모든 충전 및 사용 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AccountHistoryResponse.class))))
    })
    @GetMapping("/history")
    public ResponseEntity<List<AccountHistoryResponse>> getAccountHistory(
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo currentUser
    ) {
        List<AccountHistoryResponse> historyList = accountService.getHistoryAll(Long.parseLong(currentUser.userId()));

        return ResponseEntity.ok(historyList);
    }




    // 예치금 내역 상세(단건) 조회
    @Operation(summary = "예치금 내역 상세 조회", description = "특정 내역 ID에 해당하는 상세 거래 기록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AccountHistoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 내역 ID", content = @Content)
    })
    @GetMapping("/history/{accountHistoryId}")
    public ResponseEntity<AccountHistoryResponse> getAccountHistoryDetail(
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo currentUser,
            @Parameter(description = "예치금 내역 PK", example = "101") @PathVariable @Valid Long accountHistoryId
    ) {

        AccountHistoryResponse response = accountService.getHistory(Long.parseLong(currentUser.userId()), accountHistoryId);

        return ResponseEntity.ok(response);
    }




    // 예치 취소 (환불)
    /*
    DeleteMapping을 사용할 경우 일부 환경에서 body 무시 위험
    + 토스 api 사용 및 환불 사유 기록 로직을 수행하기 때문에 PostMapping 사용
     */
    @Operation(summary = "예치 취소 (환불)", description = "충전된 내역을 취소하고 환불 처리를 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "환불 처리 완료",
                    content = @Content(schema = @Schema(implementation = AccountHistoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "환불 가능 금액 부족 또는 기간 만료", content = @Content)
    })
    @PostMapping("/history/{accountHistoryId}/cancel")
    public ResponseEntity<AccountHistoryResponse> cancelHistory(
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo currentUser,
            @Parameter(description = "취소할 내역 PK", example = "101") @PathVariable @Valid Long accountHistoryId,
            @RequestBody @Valid TossCancelRequest request
    ) {

        AccountHistoryResponse response=accountService.deleteHistory(
                Long.parseLong(currentUser.userId()),
                accountHistoryId,
                request.cancelReason()
        );

        return ResponseEntity.ok(response);
    }


}
