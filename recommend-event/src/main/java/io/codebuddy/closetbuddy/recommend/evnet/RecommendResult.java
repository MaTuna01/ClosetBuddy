package io.codebuddy.closetbuddy.recommend.evnet;

import java.util.List;

public record RecommendResult(
        String requestId,
        Long memberId,
        boolean success,
        String failReason,
        List<RecommendResultItem> result
) {
}
