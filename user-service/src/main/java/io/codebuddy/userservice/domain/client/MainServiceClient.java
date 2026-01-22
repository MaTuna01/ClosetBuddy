package io.codebuddy.userservice.domain.client;

import io.codebuddy.userservice.domain.member.model.dto.SellerRegisterRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "closetbuddy") // Eureka에 등록된 main-service 이름
public interface MainServiceClient {

    @PostMapping("/api/v1/catalog/sellers")
    void registerSeller(@RequestHeader("X-USER-ID") Long userId,
                        @RequestBody SellerRegisterRequest request);
}