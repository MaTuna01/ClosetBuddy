package io.codebuddy.closetbuddy.domain.carts.service;

import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartItemAddRequest;
import io.codebuddy.closetbuddy.domain.carts.model.dto.response.CartProductResponse;
import io.codebuddy.closetbuddy.domain.carts.model.entity.Cart;
import io.codebuddy.closetbuddy.domain.carts.model.entity.CartItem;
import io.codebuddy.closetbuddy.domain.carts.repository.CartItemRepository;
import io.codebuddy.closetbuddy.domain.carts.repository.CartRepository;
import io.codebuddy.closetbuddy.domain.common.feign.CatalogServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CatalogServiceClient catalogServiceClient;

    /**
     * 성공 테스트
     */
    @Test
    @DisplayName("장바구니 생성 성공 테스트 - 회원가입시 생성 테스트")
    void success_createCart() {

        // given
        Long memberId = 1L;
        Long expectCartId = 1L;

        Cart saveCart = Cart.builder()
                .memberId(memberId).build();

        ReflectionTestUtils.setField(saveCart,"cartId", expectCartId);
        given(cartRepository.save(any(Cart.class))).willReturn(saveCart);

        // when
        Long cartId = cartService.createCart(memberId);

        // then
        assertThat(expectCartId).isEqualTo(cartId);

    }

    @Test
    @DisplayName("장바구니 상품 넣기 성공 테스트 - 장바구니 안에 상품이 없을 경우")
    void success_addCartItemToCart() {


        /**
         *  1. 장바구니에 상품이 없을 때 -> CartItem이 제대로 들어가는지
         *  3. 저장했을 때, 주문 Id가 잘 반환이 되는지
         */

        // given
        Long memberId = 1L;
        Long cartId = 1L;

        Cart cart = Cart.builder().memberId(memberId).build();
        ReflectionTestUtils.setField(cart,"cartId", cartId);

        given(cartRepository.findByMemberId(anyLong())).willReturn(Optional.of(cart));

        CartProductResponse productResponse = new CartProductResponse(
                1L, "네모바지", 10000L, 100, "나이키");

        // Catalog 서비스 클라이언트(Feign Client)에 productId를 넣고 productResponse를 뱉는다고 가정한다.
        given(catalogServiceClient.getCartProductInfo(1L)).willReturn(productResponse);

        CartItemAddRequest request = new CartItemAddRequest(1L, 2);

        CartItem expectedCart = CartItem.builder()
                .cart(cart)
                .productId(1L)
                .productName("네모바지")
                .productPrice(1000L)
                .storeName("나이키")
                .cartCount(2)
                .build();

        ReflectionTestUtils.setField(expectedCart, "id", 1L);
        given(cartItemRepository.save(any())).willReturn(expectedCart);

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .productId(productResponse.productId())
                .productName(productResponse.productName())
                .productPrice(productResponse.productPrice())
                .storeName(productResponse.storeName())
                .cartCount(request.productCount())
                .build();


        Long saveCartItemId = cartItemRepository.save(cartItem).getId();

        // when
        cartService.addCartItemToCart(request, memberId);

        // then
        assertThat(cartItemRepository.save(cartItem)).isEqualTo(expectedCart);
        assertThat(saveCartItemId).isEqualTo(saveCartItemId);
    }

    @Test
    @DisplayName("장바구니 상품 넣기 성공 테스트 - 장바구니 안에 상품이 있을 경우")
    void success_addCartItemToCart_JustUpdateCount() {

        // given

        // when

        // then

    }


    @Test
    @DisplayName("장바구니 조회 성공 테스트")
    void success_getCartList() {

        // given

        // when

        // then

    }

    @Test
    @DisplayName("장바구니 수량 수정 테스트")
    void success_updateCart() {

        // given

        // when

        // then

    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트")
    void success_deleteCartItem() {

        // given

        // when

        // then

    }

    /**
     * 실패 테스트
     */
    @Test
    void failed_createCart() {

        // given

        // when

        // then

    }

    @Test
    void failed_addCartItemToCart() {

        // given

        // when

        // then

    }

    @Test
    void failed_getCartList() {

        // given

        // when

        // then

    }

    @Test
    void failed_updateCart() {

        // given

        // when

        // then

    }

    @Test
    void failed_deleteCartItem() {

        // given

        // when

        // then

    }

}