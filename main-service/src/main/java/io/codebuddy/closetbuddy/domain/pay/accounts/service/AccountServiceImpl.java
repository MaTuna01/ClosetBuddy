package io.codebuddy.closetbuddy.domain.pay.accounts.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.codebuddy.closetbuddy.domain.pay.accounts.model.dto.AccountCommand;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.dto.PaymentSuccessDto;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.dto.TossPaymentResponse;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.entity.Account;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.entity.AccountHistory;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.entity.DepositCharge;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.mapper.AccountMapper;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.vo.*;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.vo.*;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.AccountHistoryRepository;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.AccountRepository;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.DepositChargeRepository;
import io.codebuddy.closetbuddy.domain.pay.exception.ErrorCode;
import io.codebuddy.closetbuddy.domain.pay.exception.PayException;
import io.codebuddy.closetbuddy.domain.pay.exception.TossErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService{

    private final ObjectMapper om;
    private final AccountRepository accountRepository;
    private final AccountHistoryRepository accountHistoryRepository;
    private final DepositChargeRepository depositChargeRepository;

    private final PaymentClient paymentClient;


    /**
     * 예치금 조회
     * @param memberId
     * @return 회원 아이디와 조회된 예치금을 리턴합니다.
     * 1. 회원 확인
     * 2. 예치금 계좌 확인
     * 3. 예치금 조회
     */
    @Override
    @Transactional(readOnly=true)
    public AccountResponse getAccountBalance(Long memberId) {

        // Member 조회 로직 제거 -> memberId로 바로 Account 조회
        // memberId는 컨트롤러에서 currentUser.userId()를 통해 가져온 값이므로 보안 작업 넣지 않음.

        Account account = accountRepository.findByMemberId(memberId).orElse(Account.createAccount(memberId));

        return AccountMapper.toResponse(account, "조회가 완료되었습니다.");
    }

    /**
     * 예치금 등록
     * @param command(meberId, 예치할 금액,paymentKey,orderId)
     * @return accountChargeResponse(예치한 금액,총 예치된 금액, 예치 일시, 예치 상태)
     *
     * 1. PG 결제 승인 요청 (먼저 수행하여 실패 시 db 접근 차단)
     * 2. 금액 검증
     * 3. 회원 검증
     * 4. 계좌 조회
     * 5. 예치금 충전
     * 6. pg 결제 내역 저장
     * 7. 예치금 내역 저장
     *
     */
    @Override
    @Transactional
    public AccountHistoryResponse charge(AccountCommand command) {
        log.info("========== [Charge Start] 요청 시작 ==========");
        log.info("Request Info - MemberId: {}, OrderId: {}, PaymentKey: {}, Amount: {}",
                command.getMemberId(), command.getOrderId(), command.getPaymentKey(), command.getAmount());

        // 1. PG 결제 승인 요청
        PaymentSuccessDto paymentSuccessDto;
        try {
            log.info("[PaymentClient] 토스 결제 승인 요청 시작...");
            paymentSuccessDto = paymentClient.confirm(command);
            log.info("[PaymentClient] 토스 승인 성공. 응답 데이터: {}", paymentSuccessDto);
        } catch (Exception e) {
            log.error("[PaymentClient] 토스 승인 요청 중 에러 발생: {}", e.getMessage(), e);
            throw e; // 에러를 상위로 던져서 트랜잭션 롤백 유도
        }

        // 2. 금액 검증
        if (!command.getAmount().equals(paymentSuccessDto.getTotalAmount())) {
            log.error("[Validation Error] 금액 불일치 - 요청 금액: {}, 승인된 금액: {}",
                    command.getAmount(), paymentSuccessDto.getTotalAmount());
            throw new PayException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        log.info("[Validation] 금액 검증 완료");

        // 3. 계좌 조회 및 생성
        log.info("[Account] 계좌 조회 시작 (MemberId: {})", command.getMemberId());
        Account account = accountRepository.findByMemberId(command.getMemberId())
                .orElseGet(() -> {
                    log.info("[Account] 계좌가 없어 신규 생성합니다.");
                    Account newAccount = Account.createAccount(command.getMemberId());
                    return accountRepository.save(newAccount);
                });

        long balanceBefore = account.getBalance();

        // 4. 충전
        account.charge(command.getAmount());
        log.info("[Account] 잔액 충전 완료. (이전: {} -> 이후: {})", balanceBefore, account.getBalance());

        // 5. 날짜 파싱
        LocalDateTime approvedAt;
        try {
            log.info("[Date Parsing] 날짜 파싱 시작. ApprovedAt String: {}", paymentSuccessDto.getApprovedAt());
            approvedAt = OffsetDateTime.parse(paymentSuccessDto.getApprovedAt())
                    .toLocalDateTime();
        } catch (Exception e) {
            log.error("[Date Parsing Error] 날짜 파싱 실패: {}", e.getMessage(), e);
            throw new RuntimeException("날짜 파싱 중 오류 발생", e);
        }

        // 6. pg 결제 내역 저장 (DepositCharge)
        log.info("[DB Save] DepositCharge 저장 시작");
        DepositCharge charge = DepositCharge.builder()
                .memberId(command.getMemberId())
                .pgPaymentKey(paymentSuccessDto.getPaymentKey())
                .pgOrderId(command.getOrderId())
                .chargeAmount(command.getAmount())
                .approvedAt(approvedAt)
                .status(ChargeStatus.DONE)
                .build();
        depositChargeRepository.save(charge);

        // 7. 예치금 내역 저장 (AccountHistory)
        log.info("[DB Save] AccountHistory 저장 시작");
        AccountHistory history = AccountHistory.builder()
                .account(account)
                .type(TransactionType.CHARGE)
                .amount(command.getAmount())
                .balanceSnapshot(account.getBalance())
                .refId(charge.getChargeId())
                .createdAt(LocalDateTime.now())
                .build();
        accountHistoryRepository.save(history);

        log.info("========== [Charge Finish] 충전 프로세스 정상 종료 (History ID: {}) ==========", history.getAccountHistoryId());
        return AccountMapper.toHistoryResponse(history);
    }

    /**
     * 예치 내역 전체 조회
     * @param memberId
     * @return 예치 내역 전체 리스트(예치한 금액, 예치한 시각, 예치 상태)
     *
     * 1. 멤버 조회
     * 2. 계좌 조회
     * 3. 예치 내역 전체 조회 (최신순)
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountHistoryResponse> getHistoryAll(Long memberId) {

        // 계좌 존재 여부 확인
        if (accountRepository.findByMemberId(memberId).isEmpty()) {
            return Collections.emptyList();
        }

        // 예치 내역 전체 조회 (최신순)
        List<AccountHistory> historyList = accountHistoryRepository.findByAccount_MemberIdOrderByCreatedAtDesc(memberId);

        return AccountMapper.toHistoryResponseList(historyList);
    }


    /**
     *
     * 예치 내역 단건 조회
     * @param memberId
     * @param historyId
     * @return 예치 내역 (예치한 금액, 예치한 시각, 예치 상태)
     *
     * 1. 멤버 검증
     * 2. 계좌 조회
     * 3. 예치 내역 단건 조회
     *
     */
    @Override
    @Transactional(readOnly = true)
    public AccountHistoryResponse getHistory(Long memberId, Long historyId) {
        // 내역 단건 조회
        // 계좌 객체(account)를 조건으로 넣어서, 남의 내역을 조회하는 것을 차단
        AccountHistory history = accountHistoryRepository.findByAccount_MemberIdAndAccountHistoryId(memberId, historyId)
                .orElseThrow(() -> new PayException(ErrorCode.ACCOUNT_HISTORY_NOT_FOUND));

        return AccountMapper.toHistoryResponse(history);
    }

    /**
     * 특정 예치 내역 삭제
     * @param memberId
     * @param historyId
     * @param reason(환불 사유) - 토스 필수 request Body 파라미터
     *
     * 1. 멤버 검증
     * 2. 계좌 검증
     * 3. 내역 조회
     * 4. pg 결제 내역 조회
     * 4. 이미 취소된 내역인지 검증
     * 5. 잔액 검증
     * 6. 계좌 잔액 차감
     * 7. 예치 내역 상태를 취소로 변경
     * 8. 토스 환불
     *
     */
    @Override
    @Transactional
    public AccountHistoryResponse deleteHistory(Long memberId, Long historyId,String reason) {

        // History랑 연결된 계좌(Account)의 주인(MemberId)이 요청한 사람(memberId)과 같은지 + 요청한 내역 번호가 맞는지 검증
        AccountHistory history = accountHistoryRepository.findByAccount_MemberIdAndAccountHistoryId(memberId, historyId)
                .orElseThrow(() -> new PayException(ErrorCode.ACCOUNT_HISTORY_NOT_FOUND));

        // 충전 건인지 확인
        if (history.getType() != TransactionType.CHARGE) {
            throw new PayException(ErrorCode.CANNOT_CANCEL_TYPE);
        }

        //계좌 객체
        Account account = history.getAccount();

        // pg 결제 내역 조회
        DepositCharge depositCharge = depositChargeRepository.findById(history.getRefId())
                .orElseThrow(() -> new PayException(ErrorCode.DEPOSIT_DATA_NOT_FOUND));

        // 이미 취소된 내역인지 검증
        if (depositCharge.getStatus() == ChargeStatus.CANCEL) {
            throw new PayException(ErrorCode.ALREADY_CANCELED_TRANSACTION);
        }

        // 잔액 검증
        if (account.getBalance() < history.getAmount()) {
            throw new PayException(ErrorCode.INSUFFICIENT_BALANCE_FOR_REFUND);
        }

        // 계좌 잔액 차감
        account.withdraw(history.getAmount());

        // DepositCharge 상태 변경
        depositCharge.cancel();

        // AccountHistory 취소 내역 추가.
        AccountHistory refundHistory = AccountHistory.builder()
                .account(account)
                .type(TransactionType.CANCEL)
                .amount(-history.getAmount()) // 음수 처리
                .balanceSnapshot(account.getBalance())
                .refId(depositCharge.getChargeId())
                .build();
        accountHistoryRepository.save(refundHistory);

        // 토스 환불 요청
        paymentClient.cancel(depositCharge.getPgPaymentKey(), reason);

        return AccountMapper.toHistoryResponse(history);
    }

}
