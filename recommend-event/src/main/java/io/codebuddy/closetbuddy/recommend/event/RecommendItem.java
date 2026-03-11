package io.codebuddy.closetbuddy.recommend.event;

public record RecommendItem(
        // fast api 서버 또한 camelCase 사용
        String imageUrl,
        String category
) {
}
