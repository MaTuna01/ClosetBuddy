package io.codebuddy.payservice.domain.pay.payments.service;

import io.codebuddy.closetbuddy.event.PaymentRequestEvent;
import io.codebuddy.closetbuddy.event.PaymentRollbackRequest;
import io.codebuddy.payservice.domain.pay.payments.model.vo.PaymentRequest;
import io.codebuddy.payservice.domain.pay.payments.model.vo.PaymentResponse;

import java.util.List;

public interface PaymentService {

    public PaymentResponse payOrder(PaymentRequestEvent event);

    public PaymentResponse payCancel(PaymentRollbackRequest event);

    PaymentResponse getPayment(Long memberId, Long orderId);

    List<PaymentResponse> getPayments(Long memberId);
}