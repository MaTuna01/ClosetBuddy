package io.codebuddy.closetbuddy.domain.recommend.controller;

import io.codebuddy.closetbuddy.domain.carts.service.RecommendResultCacheService;
import io.codebuddy.closetbuddy.domain.common.dto.RecommendProductInfoResponse;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUser;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUserInfo;
import io.codebuddy.closetbuddy.domain.common.web.dto.RecommendResult;
import io.codebuddy.closetbuddy.domain.recommend.kafka.RecommendEventProducer;
import io.codebuddy.closetbuddy.domain.recommend.service.RecommendService;
import io.codebuddy.closetbuddy.recommend.evnet.RecommendItem;
import io.codebuddy.closetbuddy.recommend.evnet.RecommendRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/carts/recommend")
public class RecommendController {

    private final RecommendEventProducer recommendEventProducer;
    private final RecommendResultCacheService cacheService;
    /**
     * 추천 요청 (kafka 비동기)
     * Kafka로 이벤트를 발행하고, 클라이언트에게 202 accepted 응답 코드와 함께
     * requestId를 즉시 반환합니다.
     */
    @Operation(
            summary = "AI 상품 추천 요청",
            description = "장바구니 상품 기반 AI 추천을 비동기로 요청합니다. "
                    + "반환된 requestId로 결과를 폴링하여 결과를 조회할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "추천 요청 접수 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    public ResponseEntity<Map<String, String>> requestRecommend(
            @CurrentUser CurrentUserInfo currentUser,
            @RequestBody List<RecommendItem> items
    ) {
        String requestId = UUID.randomUUID().toString();
        Long memberId = Long.parseLong(currentUser.userId());
        RecommendRequest event = new RecommendRequest(
                requestId,
                memberId,
                items
        );
        recommendEventProducer.sendRecommendRequest(event);
        // 202 Accepted: 비동기 처리이므로 요청은 성공했다는 응답코드
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(Map.of("requestId", requestId));
    }
    /**
     * 추천 결과 조회 (폴링을 통해 조회)
     * - 200: 결과 도착 -> 추천 상품 목록 반환
     * - 202: 아직 처리 중 -> 클라이언트가 재요청 필요
     * - 500: 추천 실패
     */
    @Operation(
            summary = "AI 추천 결과 조회",
            description = "requestId로 추천 결과를 폴링합니다. "
                    + "202 응답이면 아직 처리 중이므로 재요청하세요."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 결과 반환"),
            @ApiResponse(responseCode = "202", description = "아직 처리 중"),
            @ApiResponse(responseCode = "500", description = "추천 처리 실패")
    })
    @GetMapping("/{requestId}")
    public ResponseEntity<?> getRecommendResult(
            @PathVariable String requestId
    ) {
        return cacheService.getResult(requestId)
                .map(result -> {
                    if (result.success()) {
                        // 성공 -> 추천 결과 반환
                        return ResponseEntity.ok(result);
                    } else {
                        // 실패 -> 에러 사유 반환
                        return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", result.failReason()));
                    }
                })
                // Redis에 결과가 없음 -> 아직 AI추천 연산 진행중(실패했다면 실패 결과가 redis에 존재)
                .orElse(ResponseEntity
                        .status(HttpStatus.ACCEPTED)
                        .body(Map.of("status", "PROCESSING")));
    }
}
