package io.codebuddy.payservice.settlement;

import io.codebuddy.payservice.domain.pay.accounts.model.entity.Account;
import io.codebuddy.payservice.domain.pay.accounts.repository.AccountHistoryRepository;
import io.codebuddy.payservice.domain.pay.accounts.repository.AccountRepository;
import io.codebuddy.payservice.domain.pay.payments.repository.PaymentRepository;
import io.codebuddy.payservice.domain.settlement.model.entity.Settlement;
import io.codebuddy.payservice.domain.settlement.model.entity.SettlementRawData;
import io.codebuddy.payservice.domain.settlement.model.vo.RawDataStatus;
import io.codebuddy.payservice.domain.settlement.repository.SettlementDetailRepository;
import io.codebuddy.payservice.domain.settlement.repository.SettlementRawDataRepository;
import io.codebuddy.payservice.domain.settlement.repository.SettlementRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@Import(TestBatchConfig.class) // JobLauncherTestUtils 빈 등록을 위한 설정
class SettlementJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils; // Job 실행 도구

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountHistoryRepository accountHistoryRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private SettlementDetailRepository settlementDetailRepository;
    @Autowired
    private SettlementRawDataRepository settlementRawDataRepository;
    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void tearDown() {
        // 자식 -> 부모 데이터 삭제
        settlementDetailRepository.deleteAllInBatch();
        settlementRepository.deleteAllInBatch();
        settlementRawDataRepository.deleteAllInBatch();
        accountHistoryRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("정산 배치 통합 테스트: Job 실행 후 정산 내역 생성 및 계좌 잔액 증가 검증")
    void settlementJob_IntegrationTest() throws Exception {
        // Given
        Long sellerMemberId = 1L;
        Long initialBalance = 100000L;
        Long productPrice = 100000L;

        createAccount(sellerMemberId, initialBalance);

        String targetDateStr = LocalDate.now().toString();
        // 정산 기준: 배치 실행일로부터 3일 전 ~ 1달 전 데이터 조회
        // 5일 전으로 설정
        LocalDateTime paidAt = LocalDate.now().minusDays(5).atStartOfDay();

        SettlementRawData settlementRawData = SettlementRawData.builder()
                .paymentId(101L)
                .orderId(101L)
                .orderItemId(1L)
                .sellerId(sellerMemberId)
                .memberId(sellerMemberId)
                .storeId(50L)
                .productId(300L)
                .productName("테스트 상품")
                .productPrice(productPrice)
                .count(1)
                .orderPrice(productPrice)
                .paidAt(paidAt)
                .build();

        settlementRawDataRepository.save(settlementRawData);

        // When
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("targetDate", targetDateStr)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        // Job 성공 확인
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 정산 데이터 생성 확인
        List<Settlement> settlements = settlementRepository.findAll();
        assertThat(settlements).hasSize(1);

        Settlement settlement = settlements.get(0);
        assertThat(settlement.getTotalSalesAmount()).isEqualTo(100000L);
        assertThat(settlement.getPayoutAmount()).isEqualTo(97000L); // 수수료 3% 제외

        // 판매자 계좌 입금 확인
        Account updatedAccount = accountRepository.findByMemberId(sellerMemberId).orElseThrow();
        assertThat(updatedAccount.getBalance()).isEqualTo(197000L);
    }

    // --- Helper Methods ---

    private void createAccount(Long memberId, Long balance) {
        Account account = Account.createAccount(memberId);
        account.charge(balance);
        accountRepository.save(account);
    }

}