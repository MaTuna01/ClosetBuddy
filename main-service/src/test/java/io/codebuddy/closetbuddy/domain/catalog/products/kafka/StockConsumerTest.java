package io.codebuddy.closetbuddy.domain.catalog.products.kafka;

import io.codebuddy.closetbuddy.domain.catalog.products.exception.StockException;
import io.codebuddy.closetbuddy.domain.catalog.products.service.StockService;
import io.codebuddy.closetbuddy.event.StockCheckRequest;
import io.codebuddy.closetbuddy.event.StockCheckResult;
import io.codebuddy.closetbuddy.event.StockItem;
import io.codebuddy.closetbuddy.event.StockRollbackRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static io.codebuddy.closetbuddy.domain.catalog.products.exception.StockErrorCode.INSUFFICIENT_STOCK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockConsumer 단위 테스트")
// kafka 이벤트 consumer 테스트
class StockConsumerTest {

    @InjectMocks
    private StockConsumer stockConsumer;

    @Mock
    private StockService stockService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    // 재고 차감 성공 시 success=true 결과 이벤트 발행
    void handleStockCheckRequest_success() {
        StockCheckRequest request = new StockCheckRequest(
                10L,
                1L,
                List.of(new StockItem(1L, 2), new StockItem(2L, 1))
        );

        stockConsumer.handleStockCheckRequest(request);

        verify(stockService).deductStock(request.items());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("order.stock-check.result"), captor.capture());
        assertThat(captor.getValue()).isInstanceOf(StockCheckResult.class);

        StockCheckResult result = (StockCheckResult) captor.getValue();
        assertThat(result.orderId()).isEqualTo(10L);
        assertThat(result.success()).isTrue();
        assertThat(result.failReason()).isNull();
    }

    @Test
    // 재고 차감 실패 시 success=false 결과 이벤트 발행
    void handleStockCheckRequest_failure() {
        StockCheckRequest request = new StockCheckRequest(
                20L,
                1L,
                List.of(new StockItem(1L, 100))
        );
        doThrow(new StockException(INSUFFICIENT_STOCK)).when(stockService).deductStock(request.items());

        stockConsumer.handleStockCheckRequest(request);

        verify(stockService).deductStock(request.items());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("order.stock-check.result"), captor.capture());
        assertThat(captor.getValue()).isInstanceOf(StockCheckResult.class);

        StockCheckResult result = (StockCheckResult) captor.getValue();
        assertThat(result.orderId()).isEqualTo(20L);
        assertThat(result.success()).isFalse();
        assertThat(result.failReason()).contains("재고가 부족합니다.");
    }

    @Test
    // 재고 롤백 요청 수신 시 복구 서비스 호출
    void handleStockRollback_success() {
        StockRollbackRequest request = new StockRollbackRequest(
                30L,
                List.of(new StockItem(1L, 2))
        );

        stockConsumer.handleStockRollback(request);

        verify(stockService).restoreStock(request.items());
        verifyNoMoreInteractions(kafkaTemplate);
    }
}
