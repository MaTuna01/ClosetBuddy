package io.codebuddy.closetbuddy.recommend.event;

import java.util.List;

public record RecommendRequest(
        String requestId,
        Long memberId,
        List<RecommendItem> items
){ }
