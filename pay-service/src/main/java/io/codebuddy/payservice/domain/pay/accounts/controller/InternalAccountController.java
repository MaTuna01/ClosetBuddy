package io.codebuddy.payservice.domain.pay.accounts.controller;

import io.codebuddy.payservice.domain.pay.accounts.model.vo.AccountCreateRequest;
import io.codebuddy.payservice.domain.pay.accounts.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/accounts")
@RequiredArgsConstructor
public class InternalAccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<Void> createAccount(@RequestBody AccountCreateRequest request) {
        accountService.createAccount(request.memberId());
        return ResponseEntity.ok().build();
    }
}
