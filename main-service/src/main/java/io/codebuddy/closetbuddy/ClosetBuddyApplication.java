package io.codebuddy.closetbuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling   //배치를 위한 스케줄러 활성화
@SpringBootApplication
@ConfigurationPropertiesScan
public class ClosetBuddyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClosetBuddyApplication.class, args);
    }

}
