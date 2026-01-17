package io.codebuddy.closetbuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ClosetBuddyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClosetBuddyApplication.class, args);
    }

}
