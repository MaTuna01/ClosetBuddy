package io.codebuddy.payservice.domain.pay.accounts.service;

import io.codebuddy.payservice.domain.pay.accounts.model.dto.AccountCommand;
import io.codebuddy.payservice.domain.pay.accounts.model.dto.PaymentSuccessDto;

public interface PaymentClient {
    PaymentSuccessDto confirm(AccountCommand command);
    void cancel(String paymentKey, String reason);
}
