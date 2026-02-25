package io.codebuddy.closetbuddy.domain.recommend.kafka;

import io.codebuddy.closetbuddy.recommend.evnet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 추천 요청 이벤트 발행
     */
    public void sendRecommendRequest(RecommendRequest request) {
        log.info("AI 서비스에 추천 요청 발행: requestId={}", request.requestId());
        kafkaTemplate.send("order.recommend.request", request);
    }
}
