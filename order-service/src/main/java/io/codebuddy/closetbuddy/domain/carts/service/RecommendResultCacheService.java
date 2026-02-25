package io.codebuddy.closetbuddy.domain.carts.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.codebuddy.closetbuddy.recommend.event.RecommendResult;
import io.codebuddy.closetbuddy.recommend.evnet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendResultCacheService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String KEY_PREFIX = "recommend:result:";
    private static final Duration TTL = Duration.ofMinutes(30); // 30분 후 자동 만료 ttl 설정

    /**
     * 추천 성공 결과를 Redis에 저장
     * Python 기반의 FAST API와 통신하기 위해 json 스키마 계약에 의거하여 직렬화
     */
    public void saveResult(String requestId, RecommendResult result) {
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(KEY_PREFIX + requestId, json, TTL);
        } catch (JsonProcessingException e) {
            log.error("추천 결과 직렬화 실패: requestId={}", requestId, e);
        }
    }

    /**
     * 추천 실패 결과를 Redis에 저장
     */
    public void saveFailure(String requestId, String failReason) {
        try {
            RecommendResult failResult = new RecommendResult(
                    requestId, null, false, failReason, null
            );
            String json = objectMapper.writeValueAsString(failResult);
            redisTemplate.opsForValue().set(KEY_PREFIX + requestId, json, TTL);
        } catch (JsonProcessingException e) {
            log.error("추천 실패 결과 직렬화 실패: requestId={}", requestId, e);
        }
    }

    /**
     * 추천 결과 조회 (redis에 추천 결과를 저장해 두고 클라이언트가 조회 요청시 저장된 결과 반환)
     * - 결과가 아직 없으면 Optional.empty() 반환 -> 클라이언트에게 "처리 중" 응답
     * - 결과가 있으면 역직렬화하여 반환
     */
    public Optional<RecommendResult> getResult(String requestId) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + requestId);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, RecommendResult.class));
        } catch (JsonProcessingException e) {
            log.error("추천 결과 역직렬화 실패: requestId={}", requestId, e);
            return Optional.empty();
        }
    }
}
