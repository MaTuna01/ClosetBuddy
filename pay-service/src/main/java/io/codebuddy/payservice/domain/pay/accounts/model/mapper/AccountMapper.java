package io.codebuddy.payservice.domain.pay.accounts.model.mapper;


import io.codebuddy.payservice.domain.pay.accounts.model.entity.Account;
import io.codebuddy.payservice.domain.pay.accounts.model.entity.AccountHistory;
import io.codebuddy.payservice.domain.pay.accounts.model.vo.AccountHistoryResponse;
import io.codebuddy.payservice.domain.pay.accounts.model.vo.AccountResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountMapper {

    // Account(entity) -> AccountResponse(vo)
    public static AccountResponse toResponse(Account account, String message) {
        return new AccountResponse(
                account.getMemberId(),
                account.getBalance(),
                message
        );
    }


    // AccountHistory(Entity) -> AccountHistoryResponse(DTO)
    public static AccountHistoryResponse toHistoryResponse(AccountHistory history) {
        return new AccountHistoryResponse(
                history.getAmount(),
                history.getCreatedAt(),
                history.getType(),
                history.getBalanceSnapshot()
        );
    }

    // List<AccountHistory> -> List<AccountHistoryResponse>
    public static List<AccountHistoryResponse> toHistoryResponseList(List<AccountHistory> historyList) {
        if (historyList == null || historyList.isEmpty()) {
            return Collections.emptyList();
        }
        List<AccountHistoryResponse> responseList = new ArrayList<>();

        for (AccountHistory history : historyList) {
            responseList.add(toHistoryResponse(history));
        }

        return responseList;
    }
}
