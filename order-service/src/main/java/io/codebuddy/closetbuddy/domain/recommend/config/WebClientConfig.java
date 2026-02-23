package io.codebuddy.closetbuddy.domain.recommend.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${custom.ai.base-url}")
    private String baseUrl;

    @Value("${custom.ai.timeout}")
    private int timeout;

    @Bean
    public WebClient webClient() {
        // 타임아웃 처리
        HttpClient httpClient = HttpClient.create()
                // 서버 연결 시간 5초
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // 응답 타임아웃
                .responseTimeout(Duration.ofSeconds(timeout))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

    }

}

