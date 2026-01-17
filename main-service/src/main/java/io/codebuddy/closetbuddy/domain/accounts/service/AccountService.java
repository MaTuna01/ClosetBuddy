package io.codebuddy.closetbuddy.domain.accounts.service;


import io.codebuddy.closetbuddy.domain.accounts.model.dto.AccountCommand;
import io.codebuddy.closetbuddy.domain.accounts.model.vo.AccountHistoryResponse;
import io.codebuddy.closetbuddy.domain.accounts.model.vo.AccountResponse;

import java.util.List;

public interface AccountService {

    AccountResponse getAccountBalance(Long memberId);

    AccountHistoryResponse charge(AccountCommand command);

    List<AccountHistoryResponse> getHistoryAll(Long memberId);

    AccountHistoryResponse getHistory(Long memberId, Long historyId);

    AccountHistoryResponse deleteHistory(Long memberId, Long historyId, String reason);

}
