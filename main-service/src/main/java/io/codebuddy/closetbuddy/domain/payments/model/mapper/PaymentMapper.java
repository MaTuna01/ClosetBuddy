package io.codebuddy.closetbuddy.domain.payments.model.mapper;

import io.codebuddy.closetbuddy.domain.payments.model.entity.Payment;
import io.codebuddy.closetbuddy.domain.payments.model.vo.PaymentResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaymentMapper {

    public static PaymentResponse toPaymentResponse(Payment payment){
        return new PaymentResponse(
                payment.getPaymentAmount(),
                payment.getPaymentStatus(),
                payment.getApprovedAt(),
                payment.getUpdatedAt()
        );
    }

    public static List<PaymentResponse> toPaymentResponseList(List<Payment> payments) {
        // NullPointerException 방지
        if (payments == null || payments.isEmpty()) {
            return Collections.emptyList();
        }

        List<PaymentResponse> responseList = new ArrayList<>();

        for (Payment payment : payments) {
            responseList.add(toPaymentResponse(payment));
        }

        return responseList;
    }

}
