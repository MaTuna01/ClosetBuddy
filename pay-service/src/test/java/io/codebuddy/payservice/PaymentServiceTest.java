package io.codebuddy.payservice;


import io.codebuddy.closetbuddy.event.PaymentRequestEvent;
import io.codebuddy.closetbuddy.event.PaymentRollbackRequest;
import io.codebuddy.payservice.domain.pay.accounts.model.entity.Account;
import io.codebuddy.payservice.domain.pay.accounts.model.entity.AccountHistory;
import io.codebuddy.payservice.domain.pay.accounts.repository.AccountHistoryRepository;
import io.codebuddy.payservice.domain.pay.accounts.repository.AccountRepository;
import io.codebuddy.payservice.domain.pay.exception.PayErrorCode;
import io.codebuddy.payservice.domain.pay.exception.PayException;
import io.codebuddy.payservice.domain.pay.payments.model.entity.Payment;
import io.codebuddy.payservice.domain.pay.payments.model.vo.PaymentRequest;
import io.codebuddy.payservice.domain.pay.payments.model.vo.PaymentStatus;
import io.codebuddy.payservice.domain.pay.payments.repository.PaymentRepository;
import io.codebuddy.payservice.domain.pay.payments.service.PaymentServiceImpl;
import io.codebuddy.payservice.domain.settlement.model.entity.SettlementRawData;
import io.codebuddy.payservice.domain.settlement.model.vo.RawDataStatus;
import io.codebuddy.payservice.domain.settlement.repository.SettlementRawDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountHistoryRepository accountHistoryRepository;
    @Mock
    private SettlementRawDataRepository settlementRawDataRepository;

    @Test
    @DisplayName("결제 성공: 정상적인 요청 시 잔액이 차감되고 결제 상태가 APPROVED가 된다.")
    void payOrder_Success() {
        // given
        Long memberId=1L;
        Long orderId=1L;
        Long orderAmount=5000L;

        // Kafka Event 파라미터 세팅
        PaymentRequestEvent.OrderItemRequest mockItem = new PaymentRequestEvent.OrderItemRequest(
                10L, 20L, 30L, 40L, 1, orderAmount, "테스트 상품", "테스트 상점", "테스트 판매자"
        );
        PaymentRequestEvent event = new PaymentRequestEvent(orderId, memberId, orderAmount, List.of(mockItem));


        given(paymentRepository.existsByOrderId(event.orderId())).willReturn(false);

        given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

        Account account = Account.createAccount(memberId);
        account.charge(10000L);
        given(accountRepository.findByMemberId(memberId)).willReturn(Optional.of(account));

        given(accountHistoryRepository.save(any(AccountHistory.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        paymentService.payOrder(event);

        // then
        assertThat(account.getBalance()).isEqualTo(5000L);
        verify(paymentRepository).save(any(Payment.class));
        verify(accountHistoryRepository).save(any(AccountHistory.class));
        verify(settlementRawDataRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("결제 실패: 이미 처리된 주문 번호(중복 결제)라면 예외 발생")
    void payOrder_Fail_DuplicateOrder() {

        // given
        PaymentRequestEvent event = new PaymentRequestEvent(1L, 1L, 5000L, List.of());

        given(paymentRepository.existsByOrderId(event.orderId())).willReturn(true);

        // when & then
        assertThatThrownBy(()->paymentService.payOrder(event))
                .isInstanceOf(PayException.class)
                .hasMessage(PayErrorCode.DUPLICATE_ORDER.getMessage());


    }

    @Test
    @DisplayName("결제 실패: 잔액이 결제 금액보다 부족하면 예외 발생")
    void payOrder_Fail_NotEnoughBalance() {

        // given
        Long memberId=1L;
        PaymentRequestEvent event = new PaymentRequestEvent(1L, memberId, 5000L, List.of());

        Account account=Account.createAccount(memberId);
        account.charge(1000L);
        given(accountRepository.findByMemberId(memberId)).willReturn(Optional.of(account));

        // when & then
        assertThatThrownBy(()->paymentService.payOrder(event))
                .isInstanceOf(PayException.class)
                .hasMessage(PayErrorCode.NOT_ENOUGH_BALANCE.getMessage());


    }

    @Test
    @DisplayName("결제 취소 성공: 본인의 결제 건이며 취소 가능한 상태라면 환불된다.")
    void payCancel_Success() {

        // given
        Long memberId = 1L;
        Long orderId=1L;
        Long payAmount = 5000L;

        PaymentRollbackRequest event = new PaymentRollbackRequest(orderId, memberId);

        Payment payment = Payment.builder()
                .memberId(memberId)
                .paymentAmount(payAmount)
                .orderId(1L)
                .build();
        payment.approved();

        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));

        Account account = Account.createAccount(memberId);
        account.charge(5000L);
        given(accountRepository.findByMemberId(memberId)).willReturn(Optional.of(account));

        // when
        paymentService.payCancel(event);

        // then
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED);
        assertThat(account.getBalance()).isEqualTo(10000L);
        verify(accountHistoryRepository).save(any(AccountHistory.class));
        verify(settlementRawDataRepository).updateStatusByPaymentId(eq(orderId), eq(RawDataStatus.CANCELED));

    }

    @Test
    @DisplayName("결제 취소 실패: 본인의 결제 건이 아니면 예외 발생")
    void payCancel_Fail_NotOwner() {
        // given
        Long requesterId = 1L;    // 요청자
        Long ownerId = 2L;        // 실제 결제자
        Long orderId = 1L;
        PaymentRollbackRequest event = new PaymentRollbackRequest(orderId, requesterId);

        Payment payment = Payment.builder()
                .memberId(ownerId)
                .build();

        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> paymentService.payCancel(event))
                .isInstanceOf(PayException.class)
                .hasMessage(PayErrorCode.PAYMENT_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("결제 취소 실패: 이미 취소된 건이라면 중복 취소 불가")
    void payCancel_Fail_AlreadyCanceled() {
        // given
        Long memberId = 1L;
        Long orderId = 1L;
        PaymentRollbackRequest event = new PaymentRollbackRequest(orderId, memberId);

        Payment payment = Payment.builder()
                .memberId(memberId)
                .build();
        payment.canceled(); // 이미 취소된 상태

        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> paymentService.payCancel(event))
                .isInstanceOf(PayException.class)
                .hasMessage(PayErrorCode.ALREADY_CANCELED_TRANSACTION.getMessage());

    }

}

