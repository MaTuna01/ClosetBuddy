package io.codebuddy.closetbuddy.domain.recommend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RecommendRequest(
        @JsonProperty("image_url")
        String imageUrl,
        @JsonProperty("category")
        String categoryCode
) {
}
