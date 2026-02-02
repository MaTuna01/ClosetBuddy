package io.codebuddy.closetbuddy.domain.common.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * user-service와 통신하기 위한 Feign Client
 * Eureka를 통해 서비스 디스커버리 사용
 * 내부 API (MemberInternalController) 호출
 * Gateway를 거치지 않는 서비스 간 직접 통신
 */
@FeignClient(name = "user-service")
public interface UserServiceClient {

    /**
     * 판매자 역할 부여 요청
     * 내부 API를 통해 memberId로 직접 역할 부여
     * 
     * @param memberId 대상 회원 ID
     */
    @PostMapping("/internal/members/{memberId}/seller")
    ResponseEntity<Void> grantSellerRole(@PathVariable("memberId") Long memberId);

    /**
     * 판매자 역할 해제 요청
     * 내부 API를 통해 memberId로 직접 역할 해제
     * 
     * @param memberId 대상 회원 ID
     */
    @DeleteMapping("/internal/members/{memberId}/seller")
    ResponseEntity<Void> revokeSellerRole(@PathVariable("memberId") Long memberId);
}