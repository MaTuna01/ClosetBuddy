package io.codebuddy.closetbuddy.domain.pay.payments.service;

import io.codebuddy.closetbuddy.domain.pay.accounts.model.entity.Account;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.entity.AccountHistory;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.vo.TransactionType;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.AccountHistoryRepository;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.AccountRepository;
import io.codebuddy.closetbuddy.domain.pay.common.dto.InternalOrderItemResponse;
import io.codebuddy.closetbuddy.domain.pay.common.dto.InternalOrderResponse;
import io.codebuddy.closetbuddy.domain.pay.common.feign.OrderServiceClient;
import io.codebuddy.closetbuddy.domain.pay.exception.PayErrorCode;
import io.codebuddy.closetbuddy.domain.pay.exception.PayException;
import io.codebuddy.closetbuddy.domain.pay.payments.model.entity.Payment;
import io.codebuddy.closetbuddy.domain.pay.payments.model.mapper.PaymentMapper;
import io.codebuddy.closetbuddy.domain.pay.payments.model.vo.PaymentRequest;
import io.codebuddy.closetbuddy.domain.pay.payments.model.vo.PaymentResponse;
import io.codebuddy.closetbuddy.domain.pay.payments.model.vo.PaymentStatus;
import io.codebuddy.closetbuddy.domain.pay.payments.repository.PaymentRepository;
import io.codebuddy.closetbuddy.domain.settlement.model.entity.SettlementRawData;
import io.codebuddy.closetbuddy.domain.settlement.repository.SettlementRawDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final AccountHistoryRepository accountHistoryRepository;
    private final SettlementRawDataRepository settlementRawDataRepository;
    private final OrderServiceClient orderServiceClient;


    /**
     * 결제 수행
     * 예치금 잔액을 차감하고 결제 내역과 예치금 사용 이력에 기록합니다.
     *
     * @param memberId
     * @param request - 주문번호, 금액
     * @return 결제 금액, 결제 상태, 승인 시각, 업데이트 시각
     *
     * 1. 중복 결제 체크
     * 2. 결제 내역 생성 (PENDING)
     * 3. 예치금 잔액 차감
     * 4. 결제 승인 처리 (APPROVED)
     * 5. 예치금 사용 이력 기록
     */
    @Transactional
    @Override
    public PaymentResponse payOrder(Long memberId, PaymentRequest request) {

        // 중복 결제 방지
        if (paymentRepository.existsByOrderId(request.orderId())) {
            throw new PayException(PayErrorCode.DUPLICATE_ORDER);
        }

        // 결제 데이터 생성 (상태: PENDING)
        Payment payment = Payment.builder()
                .memberId(memberId)
                .orderId(request.orderId())
                .paymentAmount(request.amount())
                .build();
        paymentRepository.save(payment);

        // 계좌 조회 및 잔액 확인
        Account account = accountRepository.findByMemberId(memberId)
                .orElseThrow(() -> new PayException(PayErrorCode.ACCOUNT_NOT_FOUND));

        if (account.getBalance() < request.amount()) {
            throw new PayException(PayErrorCode.NOT_ENOUGH_BALANCE);
        }

        // 예치금 차감
        account.withdraw(request.amount());

        // 결제 상태 승인 변경 (PENDING -> APPROVED)
        payment.approved();

        // AccountHistory 기록
        AccountHistory history = AccountHistory.builder()
                .account(account)
                .type(TransactionType.USE)
                .amount(-request.amount())
                .balanceSnapshot(account.getBalance())
                .refId(payment.getPaymentId())        // Payment ID 저장
                .createdAt(payment.getApprovedAt())
                .build();

        accountHistoryRepository.save(history);

        // 정산 스냅샷 데이터 생성
        // 주문의 상품 목록 조회
        InternalOrderResponse orderResponse=orderServiceClient.getOrderInfo(request.orderId());

        List<SettlementRawData> rawDataList=new ArrayList<>();

        for (InternalOrderItemResponse item : orderResponse.orderItem()) {
            SettlementRawData rawData = SettlementRawData.builder()
                    .paymentId(payment.getPaymentId())
                    .orderId(request.orderId())
                    .orderItemId(item.orderItemId())
                    .sellerId(item.sellerId())
                    .memberId(memberId)
                    .storeId(item.storeId())
                    .productId(item.productId())
                    .productName(item.productName())
                    .productPrice(item.orderPrice())
                    .count(item.orderCount())
                    .orderPrice(orderResponse.orderAmount())
                    .paidAt(LocalDateTime.now())
                    .build();

            rawDataList.add(rawData);
        }

        settlementRawDataRepository.saveAll(rawDataList);

        return PaymentMapper.toPaymentResponse(payment);
    }



    /**
     * 결제 취소
     * 결제 상태를 변경(CANCELED)하고 예치금을 환불합니다.
     *
     * @param memberId
     * @param paymentId
     * @return 결제 금액, 결제 상태, 승인 시각, 업데이트 시각
     * 1. 결제 정보 조회
     * 2. 본인 확인 및 상태 검증
     * 3. 결제 상태 취소 변경 (CANCELED)
     * 4. 예치금 환불
     * 5. 환불 이력 기록 (REFUND)
     */
    @Transactional
    @Override
    public PaymentResponse payCancel(Long memberId, Long paymentId) {

        // 결제 정보 조회
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PayException(PayErrorCode.PAYMENT_NOT_FOUND));

        // 본인 확인
        if (!payment.getMemberId().equals(memberId)) {
            throw new PayException(PayErrorCode.PAYMENT_NOT_FOUND);
        }
        // 상태 확인 (이미 취소된 건인지)
        if (payment.getPaymentStatus() == PaymentStatus.CANCELED) {
            throw new PayException(PayErrorCode.ALREADY_CANCELED_TRANSACTION);
        }

        // 결제 상태 취소로 변경
        payment.canceled();

        // 계좌 조회
        Account account = accountRepository.findByMemberId(memberId)
                .orElseThrow(() -> new PayException(PayErrorCode.ACCOUNT_NOT_FOUND));

        // 환불
        long refundAmount = payment.getPaymentAmount();
        account.charge(refundAmount);

        //AccountHistory 기록 (REFUND)
        AccountHistory history = AccountHistory.builder()
                .account(account)
                .type(TransactionType.REFUND)
                .amount(refundAmount)
                .balanceSnapshot(account.getBalance())
                .refId(payment.getPaymentId())
                .createdAt(payment.getUpdatedAt())
                .build();

        accountHistoryRepository.save(history);

        return PaymentMapper.toPaymentResponse(payment);
    }

    /**
     * 결제 내역 단건 조회
     *
     * @param memberId
     * @param orderId
     * @return 결제 금액, 결제 상태, 승인 시각, 업데이트 시각
     *
     */
    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long memberId, Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PayException(PayErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getMemberId().equals(memberId)) {
            throw new PayException(PayErrorCode.PAYMENT_NOT_FOUND);
        }

        return PaymentMapper.toPaymentResponse(payment);
    }

    /**
     * 결제 내역 전체 조회
     *
     * @param memberId
     * @return List[결제 금액, 결제 상태, 승인 시각, 업데이트 시각]
     *
     */
    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPayments(Long memberId) {
        // 결제 내역 최신 순 조회
        List<Payment> payments = paymentRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);

        return PaymentMapper.toPaymentResponseList(payments);
    }
}
