package io.codebuddy.closetbuddy.domain.pay;

import io.codebuddy.closetbuddy.domain.pay.accounts.model.dto.AccountCommand;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.dto.PaymentSuccessDto;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.entity.Account;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.entity.AccountHistory;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.entity.DepositCharge;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.vo.ChargeStatus;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.vo.TransactionType;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.AccountHistoryRepository;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.AccountRepository;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.DepositChargeRepository;
import io.codebuddy.closetbuddy.domain.pay.accounts.service.AccountServiceImpl;
import io.codebuddy.closetbuddy.domain.pay.accounts.service.PaymentClient;
import io.codebuddy.closetbuddy.domain.pay.exception.PayErrorCode;
import io.codebuddy.closetbuddy.domain.pay.exception.PayException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@Slf4j
@ExtendWith(MockitoExtension.class) // Mockito 프레임워크 사용
public class AccountServiceTest {

    @InjectMocks
    private AccountServiceImpl accountService; // 테스트 대상

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountHistoryRepository accountHistoryRepository;

    @Mock
    private DepositChargeRepository depositChargeRepository;

    @Mock
    private PaymentClient paymentClient;

    @Test
    @DisplayName("예치금 충전 성공: PG 승인 후, 계좌 잔액이 증가하고 내역이 저장되어야 한다.")
    void charge_Success() {
        // given
        Long memberId = 1L;
        Long chargeAmount = 10000L;
        AccountCommand command = new AccountCommand(memberId, chargeAmount, "paymentKey", "orderId");

        // PaymentClient가 성공 응답을 준다고 가정
        PaymentSuccessDto successDto = PaymentSuccessDto.builder()
                .paymentKey("paymentKey")
                .totalAmount(chargeAmount)
                .approvedAt("2024-01-20T12:00:00+09:00")
                .build();
        given(paymentClient.confirm(command)).willReturn(successDto);

        // 계좌가 이미 존재한다고 가정 (초기 잔액 0원)
        Account account = Account.createAccount(memberId);
        given(accountRepository.findByMemberId(memberId)).willReturn(Optional.of(account));

        // save 메서드는 호출되면 입력받은 객체를 그대로 리턴한다고 가정 (DB 동작)
        given(depositChargeRepository.save(any(DepositCharge.class))).willAnswer(inv -> inv.getArgument(0));
        given(accountHistoryRepository.save(any(AccountHistory.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        accountService.charge(command);

        // then
        // 계좌 객체의 잔액이 10000원으로 변했는지 확인
        assertThat(account.getBalance()).isEqualTo(10000L);
        log.info("계좌 잔액 변동 확인 : {} ",account.getBalance ());

        verify(paymentClient).confirm(command);
        verify(depositChargeRepository).save(any(DepositCharge.class));
        verify(accountHistoryRepository).save(any(AccountHistory.class));

    }

    @Test
    @DisplayName("예치금 충전 실패: 요청 금액과 실제 결제 금액이 다르면 예외 발생")
    void charge_Fail_AmountMismatch() {
        // given
        Long requestAmount=5000L;
        Long chargeAmount=10000L;
        AccountCommand command = new AccountCommand(1L,requestAmount,"paymentKey","orderId");

        // PaymentClient가 성공 응답을 준다고 가정
        PaymentSuccessDto successDto = PaymentSuccessDto.builder()
                .totalAmount(chargeAmount)
                .build();
        given(paymentClient.confirm(command)).willReturn(successDto);

        // when & then
        assertThatThrownBy(()->accountService.charge(command))
                .isInstanceOf(PayException.class)
                .hasMessage(PayErrorCode.PAYMENT_AMOUNT_MISMATCH.getMessage());

    }

    @Test
    @DisplayName("예치금 취소(환불) 성공: 잔액이 차감되고 상태가 취소로 변경되어야 한다.")
    void deleteHistory_Success() {

        // given
        Long memberId = 1L;
        Long historyId = 100L;
        Long cancelAmount = 5000L;
        String paymentKey = "toss_key";

        // 계좌 및 히스토리 세팅
        Account account = Account.createAccount(memberId);
        account.charge(10000L); // 잔액 1만원 상태

        AccountHistory history = AccountHistory.builder()
                .account(account)
                .type(TransactionType.CHARGE)
                .amount(cancelAmount)
                .refId(200L) // DepositCharge ID
                .build();

        DepositCharge depositCharge = DepositCharge.builder()
                .chargeId(200L)
                .pgPaymentKey(paymentKey)
                .status(ChargeStatus.DONE)
                .build();

        // Mocking
        given(accountHistoryRepository.findByAccount_MemberIdAndAccountHistoryId(memberId, historyId))
                .willReturn(Optional.of(history));
        given(depositChargeRepository.findById(200L)).willReturn(Optional.of(depositCharge));

        // when
        accountService.refund(memberId, historyId, "단순 변심");

        // then
        assertThat(account.getBalance()).isEqualTo(5000L); // 10000 - 5000 = 5000
        assertThat(depositCharge.getStatus()).isEqualTo(ChargeStatus.CANCEL); // 상태 변경 확인
        log.info("계좌 잔액 : {} , 충전 상태 : {}",account.getBalance(),depositCharge.getStatus());

        verify(paymentClient).cancel(paymentKey, "단순 변심"); // 토스 취소 요청 확인

    }


    @Test
    @DisplayName("예치금 취소 실패: 잔액이 부족하면 취소될 수 있다..")
    void deleteHistory_Fail_InsufficientBalance() {
        // given
        Long memberId = 1L;
        Account account = Account.createAccount(memberId);
        // 잔액 0원 상태 (account.charge 안함)

        AccountHistory history = AccountHistory.builder()
                .account(account)
                .type(TransactionType.CHARGE)
                .amount(5000L)
                .refId(200L)
                .build();

        DepositCharge depositCharge = DepositCharge.builder()
                .status(ChargeStatus.DONE)
                .build();

        given(accountHistoryRepository.findByAccount_MemberIdAndAccountHistoryId(any(), any()))
                .willReturn(Optional.of(history));
        given(depositChargeRepository.findById(any())).willReturn(Optional.of(depositCharge));

        // when & then
        assertThatThrownBy(() -> accountService.refund(memberId, 1L, "reason"))
                .isInstanceOf(PayException.class)
                .hasMessage(PayErrorCode.INSUFFICIENT_BALANCE_FOR_REFUND.getMessage());
    }

}
