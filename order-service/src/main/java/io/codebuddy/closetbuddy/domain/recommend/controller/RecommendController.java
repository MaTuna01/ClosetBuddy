package io.codebuddy.closetbuddy.domain.recommend.controller;

import io.codebuddy.closetbuddy.domain.common.dto.RecommendProductInfoResponse;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUser;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUserInfo;
import io.codebuddy.closetbuddy.domain.common.web.dto.RecommendResult;
import io.codebuddy.closetbuddy.domain.recommend.service.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    @Operation(
            summary = "상품 추천",
            description = "사용자의 장바구니 내역을 기반으로 상품을 추천합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "추천 상품 반환 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "조회할 장바구니 상품 없음"
            )
    })
    @GetMapping("/items")
    public ResponseEntity<RecommendResult<List<RecommendProductInfoResponse>>> recommend(
            @CurrentUser CurrentUserInfo currentUser
    ) {
        List<RecommendProductInfoResponse> resultList = recommendService.getCartListForRecommend(Long.parseLong(currentUser.userId()));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(RecommendResult.success("추천 상품 반환 성공", resultList));
    }
}
