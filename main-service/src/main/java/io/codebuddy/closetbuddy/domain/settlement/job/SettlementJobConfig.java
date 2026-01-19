package io.codebuddy.closetbuddy.domain.settlement.job;

import io.codebuddy.closetbuddy.domain.payments.model.vo.PaymentStatus;
import io.codebuddy.closetbuddy.domain.settlement.model.dto.SettlementTargetDto;
import io.codebuddy.closetbuddy.domain.settlement.model.entity.SettlementDetail;
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
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private static final int CHUNK_SIZE = 1000;

    // private final SettlementItemProcessor processor;
    // private final SettlementItemWriter writer;

    // [1] Job 정의
    @Bean
    public Job settlementJob(Step settlementStep) {
        return new JobBuilder("settlementJob", jobRepository)
                .start(settlementStep)
                .build();
    }

    // [2] Step 정의
    @Bean
    @JobScope
    public Step settlementStep(JpaPagingItemReader<SettlementTargetDto> reader) {
        return new StepBuilder("settlementStep", jobRepository)
                .<SettlementTargetDto, SettlementDetail>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                // 2. Processor 자리에 임시로 로그 찍는 람다식 추가
                .processor(item -> {
                    log.info("========== [Reader 검증] 읽어온 데이터 ==========");
                    log.info("주문번호: {}, 상품명: {}, 가격: {}, 개수: {}",
                            item.getOrderId(), item.getProductName(), item.getPrice(), item.getCount());
                    log.info("===============================================");

                    // 원래는 SettlementDetail로 변환해서 리턴해야 하지만,
                    // 지금은 Writer가 없으므로 null을 리턴하면 Writer로 넘어가지 않고 여기서 끝납니다.
                    return null;
                })
                // test를 위한 writer
                .writer(items -> {
                    // 아무것도 하지 않음 (No-Op)
                })
                .build();
    }

    // [3] ItemReader 정의
    @Bean
    @StepScope
    public JpaPagingItemReader<SettlementTargetDto> settlementItemReader(
            @Value("#{jobParameters['targetDate']}") String targetDateStr) {

        LocalDate targetDate = (targetDateStr != null)
                ? LocalDate.parse(targetDateStr)
                : LocalDate.now().minusDays(1);

        LocalDateTime startDateTime = targetDate.atStartOfDay();
        LocalDateTime endDateTime = targetDate.atTime(LocalTime.MAX);

        log.info(">>> 정산 배치 Reader 시작. 대상 날짜: {}, 조회 기간: {} ~ {}", targetDate, startDateTime, endDateTime);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startDateTime", startDateTime);
        parameters.put("endDateTime", endDateTime);
        parameters.put("orderStatus", OrderStatus.COMPLETED);
        parameters.put("paymentStatus", PaymentStatus.APPROVED);

        String queryString = String.format(
                "SELECT new io.codebuddy.closetbuddy.domain.settlement.model.dto.SettlementTargetDto(" +
                        "   o.orderId, oi.id, p.productId, pay.paymentId, " +
                        "   s.sellerId, st.id, " +
                        "   p.productName, oi.orderPrice, oi.orderCount " +
                        ") " +
                        "FROM OrderItem oi " +
                        "JOIN oi.order o " +
                        "JOIN Payment pay ON pay.orderId = o.orderId " +
                        "JOIN oi.product p " +
                        "JOIN p.store st " +
                        "JOIN st.seller s " +
                        "WHERE o.updatedAt BETWEEN :startDateTime AND :endDateTime " +
                        "AND o.orderStatus = :orderStatus " +
                        "AND pay.paymentStatus = :paymentStatus " +
                        "ORDER BY oi.id ASC"
        );

        //OOM 방지
        return new JpaPagingItemReaderBuilder<SettlementTargetDto>()
                .name("settlementItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString(queryString)
                .parameterValues(parameters)
                .build();
    }
}

