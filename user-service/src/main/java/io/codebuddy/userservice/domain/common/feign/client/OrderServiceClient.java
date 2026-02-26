package io.codebuddy.userservice.domain.common.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * order-service와 통신하기 위한 Feign Client
 * Eureka를 통해 서비스 디스커버리 사용
 * 내부 API (OrderInternalController)호출
 * Gateway를 거치지 않는 서비스간 직접 통신(gateway에서 전달해 준 헤더검증을 완전히 신뢰)
 */

// url은 k8s 환경변수로 관리
// 환경변수가 없는 로컬에서는 eureka를 통해 연결
@FeignClient(name = "order-service", url = "${ORDER_SERVICE_URL:http://localhost:8087}")
public interface OrderServiceClient {

    /**
     * 장바구니 생성 요청 API
     * 
     * @param memberId 사용자 ID를 받아 해당 회원의 장바구니 생성
     *
     */
    @PostMapping("/internal/carts/{memberId}")
    ResponseEntity<Void> createMemberCart(@PathVariable("memberId") Long memberId);
}
