package io.codebuddy.closetbuddy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest // 실제 DB와 연결해서 실행
public class SettlementJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job settlementJob; // Config에 등록한 Job Bean 주입

    @Test
    @DisplayName("정산 배치를 수동으로 실행하여 로그를 확인한다")
    void runJob() throws Exception {
        // [Given] 어제 날짜를 파라미터로 생성
        String yesterday = LocalDate.now().minusDays(1).toString();

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("targetDate", yesterday)
                .addLong("time", System.currentTimeMillis()) // 중복 실행 방지용 유니크 값
                .toJobParameters();

        // [When] 배치 실행
        jobLauncher.run(settlementJob, jobParameters);

        // [Then] 콘솔 로그를 확인하세요!
    }
}


