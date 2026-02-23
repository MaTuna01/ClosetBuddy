package io.codebuddy.closetbuddy.domain.recommend;

import io.codebuddy.closetbuddy.domain.carts.model.entity.Cart;
import io.codebuddy.closetbuddy.domain.carts.model.entity.CartItem;
import io.codebuddy.closetbuddy.domain.carts.repository.CartRepository;
import io.codebuddy.closetbuddy.domain.common.dto.RecommendProductInfoResponse;
import io.codebuddy.closetbuddy.domain.common.feign.CatalogServiceClient;
import io.codebuddy.closetbuddy.domain.recommend.dto.response.RecommendResponse;
import io.codebuddy.closetbuddy.domain.recommend.exception.RecommendException;
import io.codebuddy.closetbuddy.domain.recommend.service.AIClientService;
import io.codebuddy.closetbuddy.domain.recommend.service.RecommendService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(SpringExtension.class)
public class RecommendServiceTest {

    @InjectMocks
    private RecommendService recommendService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private AIClientService aiClientService;

    @Mock
    CatalogServiceClient catalogServiceClient;

    @Test
    @DisplayName("장바구니 기반 추천 시스템 - 성공")
    void recommend_success() {
        Long memberId = 1L;
        Cart mockCart = mock(Cart.class);
        CartItem mockCartItem = mock(CartItem.class);

        // 장바구니에 100번 옷이 있다고 가정
        when(mockCartItem.getProductId()).thenReturn(100L);
        when(mockCart.getCartItems()).thenReturn(List.of(mockCartItem));
        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(mockCart));

        RecommendProductInfoResponse cartProductInfo =
                new RecommendProductInfoResponse(2L, "빨간 티셔츠",
                        100L, "s3://dummy/cart_100.jpg", "뉴발란스", "TOP");

        RecommendResponse recommendResponse =
                new RecommendResponse("s3://dummy/cart_100.jpg", "TOP", List.of("6227", "2754", "5000", "23300"));

        when(aiClientService.getRecommendation(anyList()))
                .thenReturn(List.of(recommendResponse));

        RecommendProductInfoResponse finalProduct1 =
                new RecommendProductInfoResponse(6227L, "파란 티셔츠",
                        90L, "s3://dummy/cart_6227.jpg", "나이키", "TOP");

        RecommendProductInfoResponse finalProduct2 =
                new RecommendProductInfoResponse(2754L, "보라 티셔츠",
                        1000L, "s3://dummy/cart_2754.jpg", "반스", "TOP");

        RecommendProductInfoResponse finalProduct3 =
                new RecommendProductInfoResponse(5000L, "보라 티셔츠",
                        1000L, "s3://dummy/cart_5000.jpg", "반스", "TOP");


        RecommendProductInfoResponse finalProduct4 =
                new RecommendProductInfoResponse(23300L, "보라 티셔츠",
                        1000L, "s3://dummy/cart_23300.jpg", "반스", "TOP");


        when(catalogServiceClient.getRecommendProductInfo(anyList()))
                .thenReturn(List.of(cartProductInfo))
                .thenReturn(List.of(finalProduct1, finalProduct2, finalProduct3, finalProduct4));

        // when
        List<RecommendProductInfoResponse> result = recommendService.getCartListForRecommend(memberId);

        // then
        assertThat(result).isNotNull();
        // 한 데이터당 결과값이 4개가 나왔는지
        assertThat(result).hasSize(4);

        assertThat(result.get(0)).isEqualTo(finalProduct1);
        assertThat(result.get(1)).isEqualTo(finalProduct2);
        assertThat(result.get(2)).isEqualTo(finalProduct3);
        assertThat(result.get(3)).isEqualTo(finalProduct4);
    }

    @Test
    @DisplayName("장바구니 기반 추천 시스템 실패 - 장바구니 주인이 아닐 경우")
    void recommend_fail_NotOwner() {

        Long memberId = 1L;

        // given
        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> recommendService.getCartListForRecommend(memberId))
                .isInstanceOf(RecommendException.class)
                .hasMessageContaining("사용자의 장바구니가 아닙니다.");
    }

    @Test
    @DisplayName("장바구니 기반 추천 시스템 실패 - 장바구니에 상품이 없을 경우")
    void recommend_fail_emptyCart() {

        Long memberId = 1L;
        Cart mockCart = mock(Cart.class);

        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(mockCart));
        when(mockCart.getCartItems()).thenReturn(null);

        assertThatThrownBy(() -> recommendService.getCartListForRecommend(memberId))
                .isInstanceOf(RecommendException.class)
                .hasMessageContaining("장바구니 안에 상품이 없습니다.");
    }

}
