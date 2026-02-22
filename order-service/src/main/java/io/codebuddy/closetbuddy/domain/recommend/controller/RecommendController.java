package io.codebuddy.closetbuddy.domain.recommend.controller;

import io.codebuddy.closetbuddy.domain.common.dto.RecommendProductInfoResponse;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUser;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUserInfo;
import io.codebuddy.closetbuddy.domain.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/carts/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    /**
     * 로그인 된 정보를 통해 장바구니에 있는 상품 기반으로 상품을 추천합니다.
     *
     * @param currentUser
     * @return
     */
    @GetMapping("/items")
    public ResponseEntity<List<RecommendProductInfoResponse>> recommend(
            @CurrentUser CurrentUserInfo currentUser
    ) {
        List<RecommendProductInfoResponse> resultList = recommendService.getCartListForRecommend(Long.parseLong(currentUser.userId()));

        return ResponseEntity.ok(resultList);
    }
}
