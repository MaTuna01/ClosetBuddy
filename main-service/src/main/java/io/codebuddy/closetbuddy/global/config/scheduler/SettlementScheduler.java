package io.codebuddy.closetbuddy.global.config.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final JobLauncher jobLauncher;

    // JobConfig에서 등록한 Bean 이름
    @Qualifier("settlementJob")
    private final Job settlementJob;

    // 매월 10일 00시 00분 00초에 실행
    @Scheduled(cron = "0 0 0 10 * *")
    public void runSettlementJob() {
        log.info(">>> 정산 스케줄러 시작 - 대상 날짜: {}", LocalDate.now());

        try {
            // Job Parameter 설정
            // 1. targetDate: 오늘 날짜 (배치 내부에서 날짜 계산 로직이 있으므로 실행일자만 넘김)
            // 2. time: 중복 실행 방지 및 유니크한 파라미터를 위해 현재 시간(ms) 추가
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("targetDate", LocalDate.now().toString())
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(settlementJob, jobParameters);

        } catch (JobExecutionAlreadyRunningException | JobRestartException |
                 JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            log.error("정산 배치 실행 중 오류 발생", e);
        }
    }
}