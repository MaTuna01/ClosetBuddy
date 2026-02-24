package io.codebuddy.closetbuddy.recommend.evnet;

import java.util.List;

public record RecommendResultItem(
        String imageUrl,
        String category,
        List<String> productIds  //추천된 상품 ID 목록
) {
}
