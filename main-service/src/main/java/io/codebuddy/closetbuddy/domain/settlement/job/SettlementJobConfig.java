package io.codebuddy.closetbuddy.domain.settlement.job;

import io.codebuddy.closetbuddy.domain.pay.payments.model.vo.PaymentStatus;
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
        parameters.put("orderStatus", OrderStatus.COMPLETED);
        parameters.put("paymentStatus", PaymentStatus.APPROVED);

        // join 관계가 너무 복잡하여 JPQL 사용
        String queryString = String.format(
                "SELECT new io.codebuddy.closetbuddy.domain.settlement.model.dto.SettlementTargetDto(" +
                        "   o.orderId, oi.id, p.productId, pay.paymentId, " +
                        "   s.sellerId, st.id, " +
                        "   p.productName, oi.orderPrice, oi.orderCount " +
                        ") " +
                        "FROM OrderItem oi " +  // 정산을 위한 주문 상품
                        "JOIN oi.order o " +    // 이 상품이 포함된 주문 정보
                        "JOIN Payment pay ON pay.orderId = o.orderId " +    //이 주문에 대한 결제내역
                        "JOIN oi.product p " +  //이 상품(p)의 상점(st)의 판매자(s)
                        "JOIN p.store st " +
                        "JOIN st.seller s " +
                        "WHERE o.updatedAt >= :startDateTime AND o.updatedAt < :endDateTime " + // 시작일(포함) ~ 종료일(미포함)
                        "AND o.orderStatus = :orderStatus " +   // 구매 확정 주문
                        "AND pay.paymentStatus = :paymentStatus " +     // 결제 승인
                        "ORDER BY oi.id ASC"    // PagingItemReader 사용을 위한 정렬
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

