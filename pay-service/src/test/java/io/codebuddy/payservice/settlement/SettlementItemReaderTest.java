package io.codebuddy.payservice.settlement;


import io.codebuddy.payservice.domain.settlement.model.dto.SettlementTargetDto;
import io.codebuddy.payservice.domain.settlement.model.entity.SettlementRawData;
import io.codebuddy.payservice.domain.settlement.model.vo.RawDataStatus;
import io.codebuddy.payservice.domain.settlement.repository.SettlementRawDataRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@Import(TestBatchConfig.class)
class SettlementItemReaderTest {

    @Autowired
    private JpaPagingItemReader<SettlementTargetDto> settlementItemReader;

    @Autowired
    private SettlementRawDataRepository settlementRawDataRepository;

    @AfterEach
    void tearDown() {
        settlementRawDataRepository.deleteAll();
    }

    @Test
    @DisplayName("Reader 검증: 결제일(paidAt)이 기준 기간 내이고 상태가 PAYMENT_COMPLETED인 데이터만 조회한다")
    void settlementItemReader_Test() throws Exception {
        // [Given]
        String targetDateStr = LocalDate.now().toString();

        // 1. [Valid] 정상 데이터 (5일 전 결제, PAYMENT_COMPLETED 상태)
        LocalDateTime validDate = LocalDate.now().minusDays(5).atStartOfDay();
        createRawData(100L, validDate, RawDataStatus.PAYMENT_COMPLETED, "정상_읽힘_상품");

        // 2. [Invalid] 날짜가 너무 최신 (1일 전 결제) -> 정산 대상 아님
        LocalDateTime tooRecentDate = LocalDate.now().minusDays(1).atStartOfDay();
        createRawData(200L, tooRecentDate, RawDataStatus.PAYMENT_COMPLETED, "최신_안읽힘_상품");

        // 3. [Invalid] 날짜는 맞으나 이미 정산된 상태 (SETTLED)
        createRawData(300L, validDate, RawDataStatus.SETTLED, "이미정산됨_안읽힘_상품");

        // 4. [Invalid] 날짜는 맞으나 결제 취소된 상태 (CANCELED)
        createRawData(400L, validDate, RawDataStatus.CANCELED, "취소됨_안읽힘_상품");

        // [When]
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("targetDate", targetDateStr)
                .toJobParameters();

        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);

        // [Then]
        // StepScopeTestUtils를 이용해 Reader의 read() 메서드 실행
        int readCount = StepScopeTestUtils.doInStepScope(stepExecution, () -> {
            settlementItemReader.open(new ExecutionContext());
            int count = 0;
            SettlementTargetDto item;
            try {
                while ((item = settlementItemReader.read()) != null) {
                    count++;
                    // 읽어온 데이터 검증
                    assertThat(item.getOrderId()).isEqualTo(100L);
                    assertThat(item.getProductName()).isEqualTo("정상_읽힘_상품");
                    System.out.println("Read Item: " + item.getProductName());
                }
            } finally {
                settlementItemReader.close();
            }
            return count;
        });

        // 총 4개 중 1개만 유효해야 함
        assertThat(readCount).isEqualTo(1);
    }

    // --- Helper Method ---
    private void createRawData(Long orderId, LocalDateTime paidAt, RawDataStatus targetStatus, String productName) {
        SettlementRawData rawData = SettlementRawData.builder()
                .paymentId(999L)
                .orderId(orderId)
                .orderItemId(orderId * 10)
                .sellerId(123L)
                .memberId(1L)
                .storeId(456L)
                .productId(789L)
                .productName(productName)
                .productPrice(10000L)
                .count(2)
                .orderPrice(20000L)
                .paidAt(paidAt)
                .build();

        if (targetStatus == RawDataStatus.SETTLED) {
            rawData.completeSettlement();
        } else if (targetStatus == RawDataStatus.CANCELED) {
            rawData.cancel();
        }

        settlementRawDataRepository.save(rawData);
    }
}
