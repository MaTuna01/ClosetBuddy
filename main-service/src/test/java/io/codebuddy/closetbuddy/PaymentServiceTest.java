package io.codebuddy.closetbuddy;

import io.codebuddy.closetbuddy.domain.pay.accounts.repository.AccountHistoryRepository;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.AccountRepository;
import io.codebuddy.closetbuddy.domain.pay.payments.repository.PaymentRepository;
import io.codebuddy.closetbuddy.domain.pay.payments.service.PaymentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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

    @Test
    @DisplayName("결제 성공: 정상적인 요청 시 잔액이 차감되고 결제 상태가 APPROVED가 된다.")
    void payOrder_Success() {

    }

    @Test
    @DisplayName("결제 실패: 이미 처리된 주문 번호(중복 결제)라면 예외 발생")
    void payOrder_Fail_DuplicateOrder() {

    }

    @Test
    @DisplayName("결제 실패: 잔액이 결제 금액보다 부족하면 예외 발생")
    void payOrder_Fail_NotEnoughBalance() {

    }

    @Test
    @DisplayName("결제 취소 성공: 본인의 결제 건이며 취소 가능한 상태라면 환불된다.")
    void payCancel_Success() {

    }

    @Test
    @DisplayName("결제 취소 실패: 본인의 결제 건이 아니면 예외 발생")
    void payCancel_Fail_NotOwner() {

    }

    @Test
    @DisplayName("결제 취소 실패: 이미 취소된 건이라면 중복 취소 불가")
    void payCancel_Fail_AlreadyCanceled() {

    }

}
