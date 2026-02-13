package io.codebuddy.closetbuddy.domain.orders.kafka;

import io.codebuddy.closetbuddy.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 재고 확인 이벤트 발행
     * 재고 확인 성공 후 호출
     */
    public void sendStockCheckRequest(StockCheckRequest request) {
        log.info("재고 확인 요청 발행: orderId={}", request.orderId());
        kafkaTemplate.send("order.stock-check.request", request);

    }

    /**
     * 결제 요청 이벤트 발행
     * 재고 확인 성공 후 호출
     */
    public void sendPaymentRequest(PaymentRequestEvent request) {
        log.info("결제 요청 발행: orderId={}", request.orderId());
        kafkaTemplate.send("order.payment.request", request);
    }

    /**
     * 재고 롤백 이벤트 발행
     * 결제 실패/주문 실패 시 호출되어 보상 트랜잭션 수행
     */
    public void sendStockRollback(StockRollbackRequest request) {
        log.info("재고 롤백 발행: orderId={}", request.orderId());
        kafkaTemplate.send("order.stock.rollback", request);
    }

    /**
     * 결제 롤백 이벤트 발행
     * PAID 상태에서 주문 취소 시 호출되어 보상 트랜잭션 수행
     */
    public void sendPaymentRollback(PaymentRollbackRequest request) {
        log.info("결제 롤백 발행: orderId={}", request.orderId());
        kafkaTemplate.send("order.payment.rollback", request);
    }
}
