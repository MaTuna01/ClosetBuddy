package io.codebuddy.closetbuddy.domain.settlement.job;

import io.codebuddy.closetbuddy.domain.pay.payments.model.vo.PaymentStatus;
import io.codebuddy.closetbuddy.domain.settlement.model.dto.SettlementTargetDto;
import io.codebuddy.closetbuddy.domain.settlement.model.entity.SettlementDetail;
import io.codebuddy.closetbuddy.domain.settlement.model.vo.RawDataStatus;
import io.codebuddy.closetbuddy.global.config.enumfile.OrderStatus;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private static final int CHUNK_SIZE = 100;

    private final SettlementItemProcessor processor;
    private final SettlementItemWriter writer;

    // Job 정의
    @Bean
    public Job settlementJob(Step settlementStep) {
        return new JobBuilder("settlementJob", jobRepository)
                .start(settlementStep)
                .build();
    }

    // Step 정의
    @Bean
    @JobScope
    public Step settlementStep(JpaPagingItemReader<SettlementTargetDto> reader) {
        return new StepBuilder("settlementStep", jobRepository)
                .<SettlementTargetDto, SettlementDetail>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(Exception.class) //  배치 중단 방지
                .skipLimit(10)
                .build();
    }

    // ItemReader 정의
    @Bean
    @StepScope // Late Binding : 배치의 Step이 실행되는 순간에 Bean 생성
    public JpaPagingItemReader<SettlementTargetDto> settlementItemReader(
            //Spring SpEL 사용을 통해 배치를 실행하는 순간 targetDate 설정
            @Value("#{jobParameters['targetDate']}") String targetDateStr) {

        LocalDate targetDate = (targetDateStr != null)
                ? LocalDate.parse(targetDateStr)
                : LocalDate.now();

        // 정산은 매달 10일 주문 확정된 건 (주문 완료 이후 3일)에 대하여 진행한다.
        // 조회 종료 시점 : 정산 진행일 - 3일
        LocalDateTime endDateTime = targetDate.minusDays(3).atStartOfDay();
        // 조회 시작 시점 : 종료일 - 1달
        LocalDateTime startDateTime = endDateTime.minusMonths(1);

        log.info(">>> 정산 배치 Reader 시작. 대상 날짜: {}, 조회 기간: {} ~ {}", targetDate, startDateTime, endDateTime);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startDateTime", startDateTime);
        parameters.put("endDateTime", endDateTime);
        parameters.put("status", RawDataStatus.PAYMENT_COMPLETED);

        String queryString = String.format(
                "SELECT new io.codebuddy.closetbuddy.domain.settlement.model.dto.SettlementTargetDto(" +
                        "   s.memberId, " +
                        "   s.orderId, " +
                        "   s.orderItemId, " +
                        "   s.productId, " +
                        "   s.paymentId, " +
                        "   s.sellerId, " +
                        "   s.storeId, " +
                        "   s.productName, " +
                        "   s.productPrice, " +
                        "   s.count " +
                        ") " +
                        "FROM SettlementRawData s " +
                        "WHERE s.paidAt >= :startDateTime AND s.paidAt < :endDateTime " + // 구매확정일 기준
                        "AND s.status = :status " + // PAYMENT_COMPLETED 상태
                        "ORDER BY s.settlementRawDataId ASC"    // PagingItemReader 사용을 위한 정렬
        );

        //OOM 방지를 위해 PagingItemReader 사용
        return new JpaPagingItemReaderBuilder<SettlementTargetDto>()
                .name("settlementItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString(queryString)
                .parameterValues(parameters)
                .build();
    }


}

