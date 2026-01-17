package io.codebuddy.closetbuddy.domain.payments.service;

import io.codebuddy.closetbuddy.domain.payments.model.vo.PaymentRequest;
import io.codebuddy.closetbuddy.domain.payments.model.vo.PaymentResponse;

import java.util.List;

public interface PaymentService {

    public PaymentResponse payOrder(Long memberId, PaymentRequest request);

    public PaymentResponse payCancel(Long memberId, PaymentRequest request);

    PaymentResponse getPayment(Long memberId, Long orderId);

    List<PaymentResponse> getPayments(Long memberId);
}
