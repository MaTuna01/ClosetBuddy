package io.codebuddy.closetbuddy.domain.catalog.products.kafka;

import io.codebuddy.closetbuddy.domain.catalog.products.service.StockService;
import io.codebuddy.closetbuddy.event.StockCheckRequest;
import io.codebuddy.closetbuddy.event.StockCheckResult;
import io.codebuddy.closetbuddy.event.StockRollbackRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockConsumer {

    private final StockService stockService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 재고 확인 요청 수신 리스너
     * order-service가 발행한 재고차감 요청 이벤트를 수신
     * 요청을 처리하고 결과 발행
     */
    @KafkaListener(topics = "order.stock-check.request", groupId = "catalog-service-group")
    public void handleStockCheckRequest (StockCheckRequest request) {
        log.info("재고 확인 이벤트 요청 수신: orderId = {}", request.orderId());

        try {
            // 분산락 적용된 상태에서 재고 차감 서비스로직 호출
            stockService.deductStock(request.items());
            // 성공 이벤트 결과 발행
            StockCheckResult result = new StockCheckResult(
                    request.orderId(), true, null
            );
            kafkaTemplate.send("order.stock-check.result",request);
            log.info("재고 차감 성공: orderId = {}", request.orderId());

        } catch (Exception e){

            StockCheckResult result = new StockCheckResult(
                    // 실패 결과(failReason은 추후 예외 클래스 구현 후 추가 필요)
                    request.orderId(), false, e.getMessage()
            );
            kafkaTemplate.send("order.stock-check.result",request);
            log.error("재고 차감 실패: orderId = {}, reason = {}", request.orderId(), e.getMessage());
        }
    }

    /**
     * 재고 복구 이벤트 요청 수신
     * 결제 실패/주문 취소에 따른 order-service가 보낸 보상 이벤트 처리
     *
     */
    @KafkaListener(topics = "order.stock.rollback", groupId = "catalog-service-group")
    public void handleStockRollback(StockRollbackRequest request) {
        log.info("재고 복구 요청 수신: orderId = {}", request.orderId());

        //분산락 적용 상태에서 재고 복구 서비스 메서드 호출
        stockService.restoreStock(request.items());

        log.info("재고 복구 완료: orderId = {}", request.orderId());
    }

}
