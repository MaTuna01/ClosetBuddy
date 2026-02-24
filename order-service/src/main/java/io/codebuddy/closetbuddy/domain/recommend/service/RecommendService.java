package io.codebuddy.closetbuddy.domain.recommend.service;

import io.codebuddy.closetbuddy.domain.carts.model.entity.Cart;
import io.codebuddy.closetbuddy.domain.carts.model.entity.CartItem;
import io.codebuddy.closetbuddy.domain.carts.repository.CartRepository;
import io.codebuddy.closetbuddy.domain.common.dto.RecommendProductInfoResponse;
import io.codebuddy.closetbuddy.domain.common.feign.CatalogServiceClient;
import io.codebuddy.closetbuddy.domain.recommend.dto.request.RecommendRequest;
import io.codebuddy.closetbuddy.domain.recommend.dto.response.RecommendResponse;
import io.codebuddy.closetbuddy.domain.recommend.exception.RecommendErrorCode;
import io.codebuddy.closetbuddy.domain.recommend.exception.RecommendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final CartRepository cartRepository;
    private final CatalogServiceClient catalogServiceClient;
    private final AIClientService aiClientService;

    @Transactional(readOnly = true)
    public List<RecommendProductInfoResponse> getCartListForRecommend(Long memberId) {

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RecommendException(RecommendErrorCode.NOT_OWNER));

        List<CartItem> cartItems = cart.getCartItems();

        if (cartItems == null || cartItems.isEmpty()) {
            log.info("장바구니에 상품이 없어 추천할 수 없습니다.");
            throw new RecommendException(RecommendErrorCode.CART_ITEM_NOT_FOUND);
        }

        // 장바구니에 있는 상품들의 아이디를 가져옵니다.
        List<Long> productIdsInCart = cartItems.stream()
                .map(CartItem::getProductId)
                .distinct()
                .toList();

        // 장바구니에서 이미지, 카테고리 가져오기
        List<RecommendProductInfoResponse> productInfoList = catalogServiceClient.getRecommendProductInfo(productIdsInCart);

        // AI에 보내줄 리스트 조립
        List<RecommendRequest> requests = productInfoList.stream()
                .map(info -> new RecommendRequest(
                        info.imageUrl(),
                        info.categoryCode()
                ))
                .toList();

        if(requests.isEmpty()) {
            log.info("추천에 필요한 유효한 상품이 없습니다.");
            return List.of(); // 빈 리스트 반환
        }

        // 결과값 받아오기
        List<RecommendResponse> recommendation = aiClientService.getRecommendation(requests);
        log.info("서버 응답: {}", recommendation);

        // 추천 상품 ID로 상품 정보 가져오기
        List<Long> recommendedIds = recommendation.stream()
                .flatMap(rec -> rec.productIds().stream()) // 1차원 리스트로 변경
                .map(Long::parseLong)
                .distinct()
                .toList();

        log.info("변환된 추천 상품 ID들: {}",  recommendedIds);

        if (recommendedIds.isEmpty()) {
            log.info("추천 상품이 없습니다.");
            throw new RecommendException(RecommendErrorCode.RECOMMEND_ERROR_CODE);
        }

        return catalogServiceClient.getRecommendProductInfo(recommendedIds);

    }
}
