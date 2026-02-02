package io.codebuddy.closetbuddy.domain.common.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "user-service")//판매자 등록 요청시 헤더 자동전달
public interface UserServiceClient {


    @PostMapping("/api/v1/members/me/seller")
    ResponseEntity<Void>grantSellerRole;


    @DeleteMapping("/api/v1/members/me/seller")
    ResponseEntity<Void>revokeSellerRole;
}
