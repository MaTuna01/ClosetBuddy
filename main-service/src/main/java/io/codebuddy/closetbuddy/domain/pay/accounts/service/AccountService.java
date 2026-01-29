package io.codebuddy.closetbuddy.domain.pay.accounts.service;


import io.codebuddy.closetbuddy.domain.pay.accounts.model.dto.AccountCommand;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.vo.AccountHistoryResponse;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.vo.AccountResponse;

import java.util.List;

public interface AccountService {

    AccountResponse getAccountBalance(Long memberId);

    AccountHistoryResponse charge(AccountCommand command);

    List<AccountHistoryResponse> getHistoryAll(Long memberId);

    AccountHistoryResponse getHistory(Long memberId, Long historyId);

    AccountHistoryResponse refund(Long memberId, Long historyId, String reason);

}
