package io.codebuddy.closetbuddy.domain.recommend.service;

import io.codebuddy.closetbuddy.domain.recommend.dto.request.RecommendRequest;
import io.codebuddy.closetbuddy.domain.recommend.dto.response.RecommendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIClientService {

    private final WebClient webClient;

    public List<RecommendResponse> getRecommendation(List<RecommendRequest> requests) {
        log.info("AI 서버로 추천 요청을 전송합니다. 데이터: {}", requests);
        List<RecommendResponse> responses = webClient.post()
                .uri("/v2/recommend")
                .bodyValue(requests)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<RecommendResponse>>() {})
                .block(); // 이후 비동기로 변경 필요

        if (CollectionUtils.isEmpty(responses)) {
            return Collections.emptyList();
        }

        return responses;
    }
}
