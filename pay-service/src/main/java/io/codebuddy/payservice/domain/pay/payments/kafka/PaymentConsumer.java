package io.codebuddy.payservice.domain.pay.payments.kafka;

import io.codebuddy.closetbuddy.event.PaymentRequestEvent;
import io.codebuddy.closetbuddy.event.PaymentResultEvent;
import io.codebuddy.payservice.domain.pay.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentService paymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 결제 요청 Listener
     * @param event 결제 요청 event
     * order-service가 발행한 결제 요청 수신
     */
    @KafkaListener(topics = "order.payment.request", groupId = "pay-service-group")
    public void handlePaymentRequest(PaymentRequestEvent event){

        try{
            // 결제 수행
            paymentService.payOrder(event);

            // 성공 이벤트 발행
            PaymentResultEvent resultEvent = new PaymentResultEvent(event.orderId(),true,null);

            kafkaTemplate.send("order.payment.result",resultEvent);
        } catch (Exception e) {
            // 결제 실패 이벤트 발행
            PaymentResultEvent resultEvent = new PaymentResultEvent(event.orderId(), false, e.getMessage());

            kafkaTemplate.send("order.payment.result", resultEvent);
        }

    }

}
