package io.codebuddy.closetbuddy.domain.sellers.model.dto;

import io.codebuddy.closetbuddy.domain.sellers.model.entity.Seller;

public record SellerResponse(
        Long sellerId,
        String sellerName,
        String memberEmail // 필수X
) {
    public static SellerResponse from(Seller seller) {
        return new SellerResponse(
                seller.getSellerId(),
                seller.getSellerName(),
                seller.getMember().getEmail()
        );
    }
}
