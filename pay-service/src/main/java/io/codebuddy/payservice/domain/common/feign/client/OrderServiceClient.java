package io.codebuddy.payservice.domain.common.feign.client;

import io.codebuddy.payservice.domain.common.feign.dto.InternalOrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("order-service")
public interface OrderServiceClient {

    @GetMapping("/internal/orders/{orderId}")
    InternalOrderResponse getOrderInfo(@PathVariable("orderId") Long orderId);

}