package io.codebuddy.closetbuddy.recommend.evnet;

import java.util.List;

public record RecommendResultEvent(
        String requestId,
        Long memberId,
        boolean success,
        String failReason,
        List<RecommendResultItem> result
) {
}
