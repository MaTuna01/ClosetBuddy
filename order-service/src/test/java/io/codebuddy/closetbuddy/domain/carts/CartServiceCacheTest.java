package io.codebuddy.closetbuddy.domain.carts;

import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartDeleteRequest;
import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartItemAddRequest;
import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartUpdateRequest;
import io.codebuddy.closetbuddy.domain.carts.model.dto.response.CartGetResponseDto;
import io.codebuddy.closetbuddy.domain.carts.model.entity.Cart;
import io.codebuddy.closetbuddy.domain.carts.model.entity.CartItem;
import io.codebuddy.closetbuddy.domain.carts.repository.CartItemRepository;
import io.codebuddy.closetbuddy.domain.carts.repository.CartRepository;
import io.codebuddy.closetbuddy.domain.carts.service.CartService;
import io.codebuddy.closetbuddy.domain.common.feign.CatalogServiceClient;
import io.codebuddy.closetbuddy.domain.common.feign.dto.CartProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@SpringBootTest
@ActiveProfiles("test")
class CartServiceCacheTest {

    @Autowired
    CartService cartService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    CartRepository cartRepository;

    @MockitoBean
    CartItemRepository cartItemRepository;

    @MockitoBean
    CatalogServiceClient catalogServiceClient;


    // 공통 테스트 데이터
    private final Long memberId = 1L;
    private Cart mockCart;
    private CartItem mockCartItem;
    private CartProductResponse mockProduct;

    @BeforeEach
    void setUp() {

        // 캐시 초기화
        cacheManager.getCache("cart").clear();

        mockCart = Cart.builder().memberId(memberId).build();
        ReflectionTestUtils.setField(mockCart, "cartId", 10L);

        mockCartItem = CartItem.builder()
                .cart(mockCart)
                .productId(1L)
                .cartCount(2)
                .build();

        ReflectionTestUtils.setField(mockCartItem, "id", 100L);

        mockCart.getCartItems().add(mockCartItem);

        mockProduct = new CartProductResponse(
                1L, "네모바지", 10L, "판매자 1",
                1L, "뉴발란스", 10000L, "PANTS", "square.png"
        );

        // 장바구니를 memberId로 조회하면 mockCart를 반환한다.
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(mockCart));
        // 카탈로그 Feign에 1L의 장바구니 정보를 요청했을 때, 상품 정보 (mockProduct)를 반환한다.
        given(catalogServiceClient.getCartProductInfo(1L)).willReturn(mockProduct);

    }

    @Test
    @DisplayName("캐싱: 장바구니 상품 넣기 성공 테스트")
    void success_addCartItem_cache(){

        // given
        cartService.getCartList(memberId);

        given(cartItemRepository.findByCartIdAndProductId(anyLong(), anyLong()))
                        .willReturn(Optional.empty());

        given(cartItemRepository.save(any(CartItem.class)))
                .willReturn(mockCartItem);

        // when
        cartService.addCartItemToCart(new CartItemAddRequest(2L, 1), memberId);
        cartService.getCartList(memberId);

        // then
        verify(cartRepository, times(3)).findByMemberId(memberId);
    }

    @Test
    @DisplayName("캐싱: 장바구니 상품 조회 성공 테스트")
    void success_getCart_cache(){

        // when - 같은 memberId로 두 번 조회했을 때
        List<CartGetResponseDto> responseDtoList1 = cartService.getCartList(memberId);
        List<CartGetResponseDto> responseDtoList2 = cartService.getCartList(memberId);

        // then
        assertThat(responseDtoList1).hasSize(1);
        assertThat(responseDtoList2).hasSize(1);

        verify(cartRepository, times(1)).findByMemberId(memberId);

    }

    @Test
    @DisplayName("캐싱: 장바구니 수정 테스트")
    void success_updateCart_cache(){

        // given
        cartService.getCartList(memberId);

        given(cartItemRepository.findById(100L))
                .willReturn(Optional.of(mockCartItem));

        // when
        cartService.updateCart(memberId, new CartUpdateRequest(100L, 5));
        cartService.getCartList(memberId);

        // then - 캐시가 지워졌으므로 Repository 재호출
        verify(cartRepository, times(3)).findByMemberId(memberId);
    }

    @Test
    @DisplayName("캐싱: 장바구니 개별 상품 삭제 테스트")
    void success_deleteCartItem_cache(){

        // given - 선 조회하여 캐시에 저장
        cartService.getCartList(memberId);

        // when
        cartService.deleteCartItem(memberId, new CartDeleteRequest(List.of(100L)));

        // then - 캐시가 지워졌으므로 Repository 재호출
        verify(cartRepository, times(2)).findByMemberId(memberId);
    }

    @Test
    @DisplayName("캐싱: 장바구니 삭제 테스트")
    void success_deleteCart_cache(){

        // given - 선 조회하여 캐시에 저장
        cartService.getCartList(memberId);

        // then
        cartService.deleteCart(memberId);
        cartService.getCartList(memberId);

        verify(cartRepository, times(2)).findByMemberId(memberId);

    }
}
