package io.codebuddy.closetbuddy.domain.recommend.kafka;

import io.codebuddy.closetbuddy.domain.carts.service.RecommendResultCacheService;
import io.codebuddy.closetbuddy.recommend.event.RecommendResult;
import io.codebuddy.closetbuddy.recommend.evnet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendEventConsumer {
    private final RecommendResultCacheService cacheService;
    /**
     * AI 서비스로부터 추천 결과 수신
     * 수신된 결과를 Redis에 캐싱하여 클라이언트 폴링 시 조회 가능하도록 저장
     */
    @KafkaListener(
            topics = "recommend.result",
            groupId = "order-service-group",
            properties = {
                    "spring.json.value.default.type=io.codebuddy.closetbuddy.recommend.evnet.RecommendResult",
                    "spring.json.use.type.headers=false"
            }
    )
    // 결과 수신시 redis로 캐싱
    public void saveRecommendResult(RecommendResult result) {
        log.info("추천 결과 수신: requestId={}, success={}",
                result.requestId(), result.success());
        if (result.success()) {
            cacheService.saveResult(result.requestId(), result);
            log.info("추천 결과 캐싱 완료: requestId={}, 추천 아이템 수={}",
                    result.requestId(), result.result().size());
        } else {
            cacheService.saveFailure(result.requestId(), result.failReason());
            log.warn("추천 실패: requestId={}, reason={}",
                    result.requestId(), result.failReason());
        }
    }
}
