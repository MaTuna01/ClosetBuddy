package io.codebuddy.closetbuddy.recommend.evnet;

public record RecommendItem(
        // fast api 서버 또한 camelCase 사용
        String imageUrl,
        String category
) {
}
