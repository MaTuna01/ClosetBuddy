package io.codebuddy.closetbuddy.domain.pay.common.feign;

import io.codebuddy.closetbuddy.domain.pay.common.dto.InternalOrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("order-service")
public interface OrderServiceClient {

    @GetMapping("/internal/orders/{orderId}")
    InternalOrderResponse getOrderInfo(@PathVariable("orderId") Long orderId);

}
