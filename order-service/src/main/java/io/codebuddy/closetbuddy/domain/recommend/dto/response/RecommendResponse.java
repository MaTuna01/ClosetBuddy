package io.codebuddy.closetbuddy.domain.recommend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RecommendResponse(
        @JsonProperty("image_url")
        String imageUrl,
        @JsonProperty("category")
        String categoryCode,
        @JsonProperty("product_ids")
        List<String> productIds
) {
}
