package io.codebuddy.userservice.domain.common.feign.client;

import io.codebuddy.userservice.domain.common.feign.dto.AccountCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "closetBuddy")
public interface MainServiceClient {

    // 계좌 생성 요청 API
    @PostMapping("/internal/accounts")
    void createAccount(@RequestBody AccountCreateRequest request);
}