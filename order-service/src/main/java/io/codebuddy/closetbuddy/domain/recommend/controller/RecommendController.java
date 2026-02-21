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

    @GetMapping("/items")
    public ResponseEntity<List<RecommendProductInfoResponse>> recommend(
            @CurrentUser CurrentUserInfo currentUser
    ) {
        List<RecommendProductInfoResponse> resultList = recommendService.getCartListForRecommend(Long.parseLong(currentUser.userId()));

        // 요청 보내기 성공값을 반환해야함
        return ResponseEntity.ok(resultList);
    }
}
