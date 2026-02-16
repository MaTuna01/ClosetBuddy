package io.codebuddy.closetbuddy.domain.orders.kafka;

import io.codebuddy.closetbuddy.domain.orders.exception.OrderErrorCode;
import io.codebuddy.closetbuddy.domain.orders.exception.OrderException;
import io.codebuddy.closetbuddy.domain.orders.model.entity.Order;
import io.codebuddy.closetbuddy.domain.orders.repository.OrderRepository;
import io.codebuddy.closetbuddy.event.*;
import io.codebuddy.closetbuddy.global.config.enumfile.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {
    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;
    /**
     * main-service로부터 재고 확인 결과 수신
     * 성공 시 STOCK_CONFIRMED으로 상태 변경, 결제 요청 발행
     * 실패 시 FAILED로 상태 변경 (보상 불필요)
     */
    @KafkaListener(topics = "order.stock-check.result", groupId = "order-service-group")
    @Transactional
    public void handleStockCheckResult(StockCheckResult result) {
        log.info("재고 확인 결과 수신: orderId={}, success={}", result.orderId(), result.success());
        Order order = orderRepository.findById(result.orderId())
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
        if (result.success()) {
            // 재고 차감 성공 -> 결제 요청
            order.changeStatus(OrderStatus.STOCK_CONFIRMED);
            // OrderItem → OrderItemRequest 변환
            List<PaymentRequestEvent.OrderItemRequest> itemRequests =
                    order.getOrderItem().stream()
                            .map(oi -> new PaymentRequestEvent.OrderItemRequest(
                                    oi.getOrderItemId(),
                                    oi.getSellerId(),
                                    oi.getStoreId(),
                                    oi.getProductId(),
                                    oi.getOrderCount(),
                                    oi.getOrderPrice(),
                                    oi.getProductName(),
                                    oi.getStoreName(),
                                    oi.getSellerName()
                            ))
                            .toList();
            PaymentRequestEvent paymentRequest = new PaymentRequestEvent(
                    order.getOrderId(),
                    order.getMemberId(),
                    order.getOrderAmount(),
                    itemRequests
            );
            orderEventProducer.sendPaymentRequest(paymentRequest);
            log.info("결제 요청 발행 완료: orderId={}, amount={}",
                    order.getOrderId(), order.getOrderAmount());
        } else {
            // 재고 부족 -> 주문 실패 (보상 트랜잭션 불필요)
            order.changeStatus(OrderStatus.FAILED);
            log.warn("주문 실패 (재고 부족): orderId={}, reason={}",
                    result.orderId(), result.failReason());
        }
    }
    /**
     * payment-service로부터 결제 결과 수신
     * 성공 시 PAID -> COMPLETED
     * 실패 시 FAILED 상태변경 + 재고 롤백 발행 (보상 트랜잭션 수행)
     */
    @KafkaListener(topics = "order.payment.result", groupId = "order-service-group")
    @Transactional
    public void handlePaymentResult(PaymentResultEvent result) {
        log.info("결제 결과 수신: orderId={}, success={}", result.orderId(), result.success());
        Order order = orderRepository.findById(result.orderId())
                .orElseThrow(() -> new RuntimeException(
                        "주문을 찾을 수 없습니다: orderId=" + result.orderId()));
        if (result.success()) {
            // 결제 성공 -> 주문 완료
            order.changeStatus(OrderStatus.COMPLETED);
            log.info("주문 완료: orderId={}", result.orderId());
        } else {
            // 결제 실패 -> 주문 실패 + 재고 롤백 (보상 트랜잭션수행)
            order.changeStatus(OrderStatus.FAILED);
            // 재고 롤백을 위해 OrderItem -> StockItem 변환
            List<StockItem> stockItems = order.getOrderItem().stream()
                    .map(oi -> new StockItem(oi.getProductId(), oi.getOrderCount()))
                    .toList();
            StockRollbackRequest rollbackRequest = new StockRollbackRequest(
                    order.getOrderId(), stockItems
            );
            orderEventProducer.sendStockRollback(rollbackRequest);
            log.warn("주문 실패 (결제 실패): orderId={}, reason={}. 재고 롤백 발행됨.",
                    result.orderId(), result.failReason());
        }
    }
}