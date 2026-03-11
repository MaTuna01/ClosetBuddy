package io.codebuddy.closetbuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients // Feign Client를 위한 활성화
@EnableScheduling   //배치를 위한 스케줄러 활성화
@SpringBootApplication
@ConfigurationPropertiesScan
public class ClosetBuddyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClosetBuddyApplication.class, args);
    }

}
