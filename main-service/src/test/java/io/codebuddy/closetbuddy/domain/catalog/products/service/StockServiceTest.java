package io.codebuddy.closetbuddy.domain.catalog.products.service;

import io.codebuddy.closetbuddy.domain.catalog.products.exception.StockErrorCode;
import io.codebuddy.closetbuddy.domain.catalog.products.exception.StockException;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.stores.model.entity.Store;
import io.codebuddy.closetbuddy.event.StockItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockService 단위 테스트")
class StockServiceTest {

    @InjectMocks
    private StockService stockService;

    @Mock
    private ProductJpaRepository productJpaRepository;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock1;

    @Mock
    private RLock lock2;

    private Product createProduct(Long productId, int stock) {
        return Product.builder()
                .productId(productId)
                .productName("상품" + productId)
                .productPrice(10000L)
                .productStock(stock)
                .store(mock(Store.class))
                .imageUrl("https://img.jpg")
                .category(null)
                .build();
    }

    @Test
    @DisplayName("재고 차감 성공 시 정렬된 순서로 락 획득 후 재고 차감")
    // 데드락 방지
    void deductStock_success() throws InterruptedException {
        Product product1 = createProduct(1L, 10);
        Product product2 = createProduct(2L, 8);
        List<StockItem> items = List.of(new StockItem(2L, 3), new StockItem(1L, 2));

        when(redissonClient.getLock("lock:product:1")).thenReturn(lock1);
        when(redissonClient.getLock("lock:product:2")).thenReturn(lock2);
        when(lock1.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock2.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock1.isHeldByCurrentThread()).thenReturn(true);
        when(lock2.isHeldByCurrentThread()).thenReturn(true);
        when(productJpaRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productJpaRepository.findById(2L)).thenReturn(Optional.of(product2));

        stockService.deductStock(items);

        assertThat(product1.getProductStock()).isEqualTo(8);
        assertThat(product2.getProductStock()).isEqualTo(5);

        InOrder inOrder = inOrder(redissonClient);
        inOrder.verify(redissonClient).getLock("lock:product:1");
        inOrder.verify(redissonClient).getLock("lock:product:2");

        verify(lock1).unlock();
        verify(lock2).unlock();
    }

    @Test
    @DisplayName("재고 차감 중 락 획득 실패 시 LOCK_ACQUISITION_FAILURE 예외")
    void deductStock_lockAcquisitionFailure() throws InterruptedException {
        List<StockItem> items = List.of(new StockItem(1L, 1), new StockItem(2L, 1));

        when(redissonClient.getLock("lock:product:1")).thenReturn(lock1);
        when(redissonClient.getLock("lock:product:2")).thenReturn(lock2);
        when(lock1.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock2.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);
        when(lock1.isHeldByCurrentThread()).thenReturn(true);

        assertThatThrownBy(() -> stockService.deductStock(items))
                .isInstanceOf(StockException.class)
                .satisfies(ex -> assertThat(((StockException) ex).getErrorCode())
                        .isEqualTo(StockErrorCode.LOCK_ACQUISITION_FAILURE));

        verify(lock1).unlock();
        verify(lock2, never()).unlock();
    }

    @Test
    @DisplayName("재고 부족 시 INSUFFICIENT_STOCK 예외")
    void deductStock_insufficientStock() throws InterruptedException {
        Product product1 = createProduct(1L, 0);
        List<StockItem> items = List.of(new StockItem(1L, 1), new StockItem(2L, 1));

        when(redissonClient.getLock("lock:product:1")).thenReturn(lock1);
        when(redissonClient.getLock("lock:product:2")).thenReturn(lock2);
        when(lock1.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock2.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock1.isHeldByCurrentThread()).thenReturn(true);
        when(lock2.isHeldByCurrentThread()).thenReturn(true);
        when(productJpaRepository.findById(1L)).thenReturn(Optional.of(product1));

        assertThatThrownBy(() -> stockService.deductStock(items))
                .isInstanceOf(StockException.class)
                .satisfies(ex -> assertThat(((StockException) ex).getErrorCode())
                        .isEqualTo(StockErrorCode.INSUFFICIENT_STOCK));

        verify(lock1).unlock();
        verify(lock2).unlock();
    }

    @Test
    @DisplayName("재고 복구 성공 시 수량 원복")
    void restoreStock_success() throws InterruptedException {
        Product product1 = createProduct(1L, 2);
        Product product2 = createProduct(2L, 5);
        List<StockItem> items = List.of(new StockItem(2L, 3), new StockItem(1L, 4));

        when(redissonClient.getLock("lock:product:1")).thenReturn(lock1);
        when(redissonClient.getLock("lock:product:2")).thenReturn(lock2);
        when(lock1.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock2.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock1.isHeldByCurrentThread()).thenReturn(true);
        when(lock2.isHeldByCurrentThread()).thenReturn(true);
        when(productJpaRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productJpaRepository.findById(2L)).thenReturn(Optional.of(product2));

        stockService.restoreStock(items);

        assertThat(product1.getProductStock()).isEqualTo(6);
        assertThat(product2.getProductStock()).isEqualTo(8);
        verify(lock1).unlock();
        verify(lock2).unlock();
    }
}
