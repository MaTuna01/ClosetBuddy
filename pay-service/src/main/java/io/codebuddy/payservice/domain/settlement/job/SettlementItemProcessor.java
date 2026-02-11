package io.codebuddy.payservice.domain.settlement.job;

import io.codebuddy.payservice.domain.settlement.model.dto.SettlementTargetDto;
import io.codebuddy.payservice.domain.settlement.model.entity.SettlementDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Slf4j
@Component // Bean으로 등록하여 JobConfig에서 주입받아 사용
public class SettlementItemProcessor implements ItemProcessor<SettlementTargetDto, SettlementDetail> {

    private final double feeRate;

    // yml 을 통해 수수료 설정 - 모든 판매자에게 동일한 수수료를 적용
    public SettlementItemProcessor(@Value("${custom.settlement.fee-rate}") double feeRate) {
        this.feeRate = feeRate;
    }

    /**
     * 정산 기능을 수행합니다.
     * @param item
     * @return
     * @throws Exception
     *
     * 정산에는 ItemReader에서 조회한 데이터를 통해 진행합니다.
     * 1. 총 매출액 계산 (가격 * 수량)
     * 2. 수수료 계산 (총액 * 수수료율 / 100)
     * 3. 정산 지급액 계산 (총액 - 수수료)
     * 4. Entity 변환
     *
     */
    @Override
    public SettlementDetail process(SettlementTargetDto item) throws Exception {
        // 1. 총 매출액 계산 (가격 * 수량)
        long totalAmount = item.getPrice() * item.getCount();

        // 2. 수수료 계산 (총액 * 수수료율 / 100)
        long feeAmount = (long) (totalAmount * (feeRate / 100.0));

        // 3. 정산 지급액 계산 (총액 - 수수료)
        long payoutAmount = totalAmount - feeAmount;

        // 4. 로그 확인
        log.info("정산 계산 - 주문ID: {}, 총액: {}, 수수료({}%): {}, 지급액: {}",
                item.getOrderId(), totalAmount, feeRate, feeAmount, payoutAmount);

        // 5. Entity 변환
        return SettlementDetail.builder()
                .memberId(item.getMemberId())
                .orderId(item.getOrderId())
                .orderItemId(item.getOrderItemId())
                .productId(item.getProductId())
                .paymentId(item.getPaymentId())
                .settlementRawDataId(item.getSettlementRawDataId())
                .productName(item.getProductName())
                .productPrice(item.getPrice())
                .quantity(item.getCount())
                .totalAmount(totalAmount)
                .feeRate(BigDecimal.valueOf(feeRate))
                .feeAmount(feeAmount)
                .payoutAmount(payoutAmount)
                .storeId(item.getStoreId())
                .sellerId(item.getSellerId())
                .createdAt(LocalDateTime.now())
                .build();
    }
}