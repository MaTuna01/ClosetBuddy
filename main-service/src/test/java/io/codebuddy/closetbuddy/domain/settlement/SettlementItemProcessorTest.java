package io.codebuddy.closetbuddy.domain.settlement;

import io.codebuddy.closetbuddy.domain.settlement.job.SettlementItemProcessor;
import io.codebuddy.closetbuddy.domain.settlement.model.dto.SettlementTargetDto;
import io.codebuddy.closetbuddy.domain.settlement.model.entity.SettlementDetail;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SettlementItemProcessorTest {

    @Test
    void calculatesSettlementAmountsFromOrderItem() throws Exception {
        SettlementItemProcessor processor = new SettlementItemProcessor(10.0);

        SettlementTargetDto target = new SettlementTargetDto(
                101L,
                101L,
                201L,
                301L,
                401L,
                501L,
                601L,
                "테스트 상품",
                20000L,
                3
        );

        SettlementDetail result = processor.process(target);

        assertThat(result.getTotalAmount()).isEqualTo(60000L);
        assertThat(result.getFeeAmount()).isEqualTo(6000L);
        assertThat(result.getPayoutAmount()).isEqualTo(54000L);
        assertThat(result.getFeeRate()).isEqualTo(BigDecimal.valueOf(10.0));
        assertThat(result.getOrderId()).isEqualTo(101L);
        assertThat(result.getOrderItemId()).isEqualTo(201L);
        assertThat(result.getProductId()).isEqualTo(301L);
        assertThat(result.getPaymentId()).isEqualTo(401L);
        assertThat(result.getStoreId()).isEqualTo(601L);
        assertThat(result.getSellerId()).isEqualTo(501L);
    }
}