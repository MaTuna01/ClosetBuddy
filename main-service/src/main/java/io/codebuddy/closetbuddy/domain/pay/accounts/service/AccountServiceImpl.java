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

    @Value("${custom.payments.toss.secrets}")
    private String tossPaymentSecrets;

    @Value("${custom.payments.toss.url}")
    private String tossPaymentUrl;

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

        // PG 결제 승인 요청
        PaymentSuccessDto paymentSuccessDto = confirmTossPayment(command);

        // 금액 검증
        // 요청한 금액과 실제 결제된 금액이 다르면 예외 발생
        if (!command.getAmount().equals(paymentSuccessDto.getTotalAmount()) ) {
            throw new PayException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 회원 검증 (MemberRepository 제거됨) -> 회원검증은 GateWay에서 진행하므로 별다른 검증 진행 X

        // 계좌 조회
        Account account = accountRepository.findByMemberId(command.getMemberId())
                .orElseGet(() -> {
                    Account newAccount = Account.createAccount(command.getMemberId());
                    return accountRepository.save(newAccount);
                });


        // 충전
        account.charge(command.getAmount());

        // 날짜 파싱 (String -> LocalDateTime)
        // 토스는 ISO 8601 (2022-01-01T00:00:00+09:00) 형식
        LocalDateTime approvedAt = OffsetDateTime.parse(paymentSuccessDto.getApprovedAt())
                .toLocalDateTime();

        //pg 결제 내역 저장
        DepositCharge charge = DepositCharge.builder()
                .memberId(command.getMemberId())
                .pgPaymentKey(paymentSuccessDto.getPaymentKey())
                .pgOrderId(command.getOrderId())
                .chargeAmount(command.getAmount())
                .status(ChargeStatus.DONE)
                .build();
        depositChargeRepository.save(charge);

        // 예치금 내역 저장
        AccountHistory history = AccountHistory.builder()
                .account(account)
                .type(TransactionType.CHARGE)
                .amount(command.getAmount())
                .balanceSnapshot(account.getBalance())
                .refId(charge.getChargeId()) // DepositCharge의 ID를 저장
                .createdAt(LocalDateTime.now())
                .build();

        accountHistoryRepository.save(history);

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
        cancelTossPayment(depositCharge.getPgPaymentKey(), reason);

        return AccountMapper.toHistoryResponse(history);
    }

    // 토스 결제 취소
    private void cancelTossPayment(String paymentKey, String cancelReason) {
        try {
            // 인증 헤더 (시크릿 키)
            String authorization = "Basic " + Base64.getEncoder()
                    .encodeToString((tossPaymentSecrets + ":").getBytes(StandardCharsets.UTF_8));

            // 요청 Body 생성 (Record -> JSON)
            String requestBody = om.writeValueAsString(new TossCancelRequest(cancelReason));

            // HttpClient 준비
            HttpClient client = HttpClient.newHttpClient();

            // 요청 만들기 (POST)
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(tossPaymentUrl + "/" + paymentKey + "/cancel"))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 전송
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // 토스 결제 취소 실패
            if (response.statusCode() != 200) {
                TossErrorResponse errorResponse = om.readValue(response.body(), TossErrorResponse.class);

                log.error("토스 환불 실패. 상태코드: {}, 내용: {}", response.statusCode(), response.body());

                if (response.statusCode() >= 400 && response.statusCode() < 500) {
                    // 토스 서버로부터 4xx
                    throw new PayException(ErrorCode.PAYMENT_APPROVAL_FAILED, errorResponse.getMessage());
                } else {
                    // 토스 서버로부터 5xx
                    throw new PayException(ErrorCode.PAYMENT_SYSTEM_ERROR);
                }
            }

            log.info("토스 결제 취소 성공. paymentKey: {}", paymentKey);

        } catch (PayException e) {
            // PayException은 Exception이 잡지 않도록 함
            throw e;
        } catch (Exception e) { // 여기서 예외를 던져야 deleteHistory의 @Transactional이 작동해 DB도 롤백
            log.error("토스 결제 취소 중 통신 오류 발생", e);
            // 그 외(JsonProcessingException, IOException 등)만 RuntimeException으로 감쌉니다.
            throw new RuntimeException("결제 취소 연동 중 오류가 발생했습니다.", e);
        }


    }


    // 토스 결제 검증
    private PaymentSuccessDto confirmTossPayment(AccountCommand command) {
        try {
            // 1. 시크릿 키 인코딩
            String authorization = "Basic " + Base64.getEncoder()
                    .encodeToString((tossPaymentSecrets + ":").getBytes(StandardCharsets.UTF_8));

            // 2. 요청 객체를 JSON 문자열로 변환
            String requestBody = om.writeValueAsString(
                    new TossPaymentConfirm(
                            command.getPaymentKey(),
                            command.getOrderId(),
                            command.getAmount()));

            // 3. HttpClient 생성
            HttpClient client = HttpClient.newHttpClient();

            // 4. 요청 생성
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(tossPaymentUrl + "/confirm"))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 5. 요청 전송 및 응답 수신
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // 6. 결과 처리
            if (response.statusCode() == 200) {
                // 성공 시: json 문자열 -> dto로 매핑
                TossPaymentResponse tossResponse = om.readValue(response.body(), TossPaymentResponse.class);
                return PaymentSuccessDto.builder()
                        .paymentKey(tossResponse.getPaymentKey())
                        .status(tossResponse.getStatus())
                        .totalAmount(tossResponse.getTotalAmount())
                        .approvedAt(tossResponse.getApprovedAt())
                        .build();
            } else {
                // 실패 시: 에러 로그 출력 및 예외 발생
                log.error("토스 결제 승인 실패. 응답코드: {}, 내용: {}", response.statusCode(), response.body());

                TossErrorResponse errorResponse = om.readValue(response.body(), TossErrorResponse.class);

                if (response.statusCode() >= 400 && response.statusCode() < 500) {
                    // 토스 서버로부터 4xx
                    throw new PayException(ErrorCode.PAYMENT_APPROVAL_FAILED, errorResponse.getMessage());
                } else {
                    // 토스 서버로부터 5xx
                    throw new PayException(ErrorCode.PAYMENT_SYSTEM_ERROR);
                }
            }

        } catch (PayException e) {
            throw e;
        } catch (Exception e) {
            log.error("토스 결제 통신 중 에러 발생", e);
            throw new RuntimeException("결제 시스템 연동 중 오류가 발생했습니다.", e);
        }
    }
}
