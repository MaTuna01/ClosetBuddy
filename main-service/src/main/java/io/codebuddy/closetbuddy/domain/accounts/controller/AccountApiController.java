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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Account", description = "예치금 조회, 충전 및 결제 내역 관리 API")
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountApiController {

    private final AccountService accountService;

    //메서드 2개 추가
    private Long currentMemberId(CurrentUserInfo currentUser) {
        return Long.parseLong(currentUser.userId());
    }

    private void assertOwner(CurrentUserInfo currentUser, Long memberId) {
        Long currentId = currentMemberId(currentUser);
        if (!currentId.equals(memberId)) {
            throw new AccessDeniedException("본인 계좌만 접근할 수 있습니다.");
        }
    }

    //예치금 조회
    @Operation(summary = "예치금 잔액 조회", description = "현재 로그인한 사용자의 예치금 잔액 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @GetMapping("/{memberId}/me")
    public ResponseEntity<AccountResponse> getAccountBalance(
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo currentUser,
            @PathVariable Long memberId
    ){
        assertOwner(currentUser, memberId);
        return ResponseEntity.ok(accountService.getAccountBalance(memberId));
    }

    //예치금 등록
    @Operation(summary = "예치금 충전(결제 승인)", description = "토스페이먼츠 결제 성공 후 전달받은 정보를 바탕으로 예치금을 충전합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "충전 완료",
                    content = @Content(schema = @Schema(implementation = AccountHistoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "결제 정보 불일치 또는 유효하지 않은 요청", content = @Content)
    })
    @PostMapping("/{memberId}/charge")
    public ResponseEntity<AccountHistoryResponse> chargeAccount(
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo currentUser,
            @PathVariable Long memberId,
            @RequestBody PaymentConfirmRequest request
    ) {

        assertOwner(currentUser, memberId);

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
    @Operation(summary = "예치금 내역 전체 조회", description = "현재 사용자의 모든 충전 및 사용 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AccountHistoryResponse.class))))
    })
    @GetMapping("/{memberId}/history")
    public ResponseEntity<List<AccountHistoryResponse>> getAccountHistory(
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo currentUser,
            @PathVariable Long memberId
    ) {
        assertOwner(currentUser, memberId);

        return ResponseEntity.ok(accountService.getHistoryAll(memberId));
    }

    // 예치금 내역 상세(단건) 조회
    @Operation(summary = "예치금 내역 상세 조회", description = "특정 내역 ID에 해당하는 상세 거래 기록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AccountHistoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 내역 ID", content = @Content)
    })
    @GetMapping("/{memberId}/history/{accountHistoryId}")
    public ResponseEntity<AccountHistoryResponse> getAccountHistoryDetail(
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo currentUser,
            @PathVariable Long memberId,
            @Parameter(description = "예치금 내역 PK", example = "101") @PathVariable Long accountHistoryId
    ) {

        assertOwner(currentUser, memberId);

        return ResponseEntity.ok(accountService.getHistory(memberId, accountHistoryId));
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
    @PostMapping("/{memberId}/history/{accountHistoryId}/cancel")
    public ResponseEntity<AccountHistoryResponse> cancelHistory(
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo currentUser,
            @PathVariable Long memberId,
            @Parameter(description = "취소할 내역 PK", example = "101") @PathVariable Long accountHistoryId,
            @RequestBody @Valid TossCancelRequest request
    ) {

        assertOwner(currentUser, memberId);
        AccountHistoryResponse response=accountService.deleteHistory(
                memberId,
                accountHistoryId,
                request.cancelReason()
        );

        return ResponseEntity.ok(response);
    }


}

/*
memberid : 사용자가 조작할 수 있는 id
currentUser: 인증된 사용자
사용자가 memberid를 입력/조작 할 수 있으므로 컨트롤러에서 currentUser.userId()와 memberId를 비교해서 같을 때만 통과시키고 통과 후에만
서비스에 memberId를 넘긴다.
memberId를 넘기는 이유: 컨트롤러에서 asserOwner()로 요청자(currentUser)와 대상(memberId)이 같은지 검증을 한 뒤, 그 검증된
memberId로 서비스에 넘기는 게 정상 패턴.
 */