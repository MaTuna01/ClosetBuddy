package io.codebuddy.userservice.domain.common.feign.client;

import io.codebuddy.userservice.domain.common.feign.dto.AccountCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


// url은 k8s 환경변수로 관리
// 환경변수가 없는 로컬에서는 eureka를 통해 연결
@FeignClient(name = "pay-service", url = "${PAY_SERVICE_URL:http://localhost:8088}")
public interface PayServiceClient {

    // 계좌 생성 요청 API
    @PostMapping("/internal/accounts")
    void createAccount(@RequestBody AccountCreateRequest request);
}