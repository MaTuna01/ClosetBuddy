package io.codebuddy.closetbuddy.domain.recommend.dto.response;

import io.codebuddy.closetbuddy.recommend.evnet.RecommendResultItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecommendPollingResponse {
    private final String status; //"PROCESSING" | "COMPLETED" | "FAILED"
    private final String message; //상태 메시지 또는 실패 사유
    private final List<RecommendResultItem> data; // 추천 결과(COMPLETED)일 경우

    //Processing
    public static RecommendPollingResponse processing() {
        return new RecommendPollingResponse("PROCESSING", "추천 결과를 처리중입니다.", null);
    }

    //success
    public static RecommendPollingResponse completed(List<RecommendResultItem> result) {
        return new RecommendPollingResponse("COMPLETED", "추천 상품입니다.", result);
    }

    //failed
    public static RecommendPollingResponse failed(String reason) {
        return new RecommendPollingResponse("FAILED", reason, null);
    }
}
