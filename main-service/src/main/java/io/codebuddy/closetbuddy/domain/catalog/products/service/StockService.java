package io.codebuddy.closetbuddy.domain.catalog.products.service;

import io.codebuddy.closetbuddy.domain.catalog.products.exception.StockErrorCode;
import io.codebuddy.closetbuddy.domain.catalog.products.exception.StockException;
import io.codebuddy.closetbuddy.event.StockItem;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductJpaRepository productJpaRepository;
    private final RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "lock:product:";
    private static final long WAIT_TIME = 5L;   // 락 대기 시간 (초)
    private static final long LEASE_TIME = 3L;   // 락 보유 시간 (초)

    /**
     * 분산락 기반 재고 차감
     * 1. productId 오름차순 정렬 (데드락 방지)
     * 2. 각 상품에 대해 분산락 획득
     * 3. 재고 확인 후 차감
     * 4. finally에서 모든 락 해제
     */
    @Transactional
    public void deductStock(List<StockItem> items) {
        // 데드락 방지를 위한 productId 오름차순 정렬
        List<StockItem> sortedItems = items.stream()
                .sorted(Comparator.comparing(StockItem::productId))
                .toList();

        List<RLock> acquiredLocks = new ArrayList<>();

        try {
            // 모든 상품에 대해 락 획득 시도
            for (StockItem item : sortedItems) {
                RLock lock = redissonClient.getLock(LOCK_PREFIX + item.productId());

                // tryLock 대기중 서버 다운이나 종료 요청 시 InterruptedException 발행 -> RuntimeException으로 처리
                boolean acquired = lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
                // 락 획득 실패시 예외 발생 추후 커스텀 예외 적용
                if (!acquired) {
                    throw new StockException(
                            StockErrorCode.LOCK_ACQUISITION_FAILURE);
                }
                acquiredLocks.add(lock);
            }

            // 모든 락 획득 성공 → 재고 확인 & 차감 수행
            for (StockItem item : sortedItems) {
                Product product = productJpaRepository.findById(item.productId())
                        // 상품을 찾을 수 없을 때 productException 예외
                        .orElseThrow(() -> new StockException(StockErrorCode.PRODUCT_NOT_FOUND));

                if (product.getProductStock() < item.quantity()) {
                    // 재고 차감중 재고 부족 예외처리 -> 추후 커스텀 Exception 구현 후 수정 필수
                    throw new StockException(
                            StockErrorCode.INSUFFICIENT_STOCK
//                            "재고 부족: " + product.getProductName()
//                                    + " (남은 수량: " + product.getProductStock()
//                                    + ", 요청: " + item.quantity() + ")"
                    );
                }
                // 재고 차감
                product.setProductStock(product.getProductStock() - item.quantity());
                log.info("재고 차감 완료: productId={}, 차감수량={}, 남은재고={}",
                        item.productId(), item.quantity(), product.getProductStock());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // RuntimeException을 발생시킨 부분은 추후 커스텀 Exception 구현 후 변경 예정
            throw new StockException(StockErrorCode.LOCK_INTERRUPTED);

        } finally {
            // 획득한 모든 락 해제
            for (RLock lock : acquiredLocks) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }

    /**
     * 재고 복구 (보상 트랜잭션용)
     * Saga 실패 시 차감했던 재고를 원복
     */
    @Transactional
    public void restoreStock(List<StockItem> items) {
        List<StockItem> sortedItems = items.stream()
                .sorted(Comparator.comparing(StockItem::productId))
                .toList();
        List<RLock> acquiredLocks = new ArrayList<>();
        // 재고 차감 로직과 동일하게 복구 또한 락을 걸어 데이터 정합성/무결성 보장
        try {
            for (StockItem item : sortedItems) {
                RLock lock = redissonClient.getLock(LOCK_PREFIX + item.productId());
                boolean acquired = lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
                if (!acquired) {
                    throw new StockException(StockErrorCode.LOCK_ACQUISITION_FAILURE);
                }
                acquiredLocks.add(lock);
            }
            for (StockItem item : sortedItems) {
                Product product = productJpaRepository.findById(item.productId())
                        .orElseThrow(() -> new StockException(StockErrorCode.PRODUCT_NOT_FOUND));
                product.setProductStock(product.getProductStock() + item.quantity());
                log.info("재고 복구 완료: productId={}, 복구수량={}, 현재재고={}",
                        item.productId(), item.quantity(), product.getProductStock());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StockException(StockErrorCode.LOCK_INTERRUPTED);
        } finally {
            for (RLock lock : acquiredLocks) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }
}