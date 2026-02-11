package io.codebuddy.closetbuddy.domain.orders.controller;

import io.codebuddy.closetbuddy.domain.orders.model.dto.response.InternalOrderResponse;
import io.codebuddy.closetbuddy.domain.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/internal/orders")
@RestController
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<InternalOrderResponse> getOrderInfo(@PathVariable Long orderId){
        InternalOrderResponse internalOrder = orderService.getInternalOrder(orderId);

        return ResponseEntity.ok(internalOrder);
    }

}
