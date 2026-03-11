package io.codebuddy.closetbuddy.domain.carts;

import io.codebuddy.closetbuddy.domain.carts.exception.CartErrorCode;
import io.codebuddy.closetbuddy.domain.carts.exception.CartException;
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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Slf4j
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

        @Test
        @DisplayName("장바구니 생성 성공 테스트 - 회원가입시 생성 테스트")
        void success_createCart() {

                // given
                Long memberId = 1L;
                Long expectCartId = 1L;

                given(cartRepository.saveAndFlush(any(Cart.class)))
                                .willAnswer(invocation -> {
                                        Cart mockCart = invocation.getArgument(0);
                                        ReflectionTestUtils.setField(mockCart, "cartId", expectCartId);
                                        return mockCart;
                                });

                // when
                Long resultId = cartService.createCart(memberId);

                // then
                assertThat(resultId).isEqualTo(expectCartId);

                verify(cartRepository).saveAndFlush(argThat(cart -> cart.getMemberId().equals(memberId)));

        }

        @Test
        @DisplayName("장바구니 상품 넣기 성공 테스트 - 장바구니 안에 상품이 없을 경우")
        void success_addCartItemToCart() {

                // 장바구니에 상품이 없을 때 -> CartItem이 제대로 들어가는지
                // 저장했을 때, 주문 Id가 잘 반환이 되는지

                // given
                Long memberId = 1L;
                Long productId = 1L;
                Long expectedCartItemId = 100L;
                Integer requestCount = 10;

                // CartItemAddRequest에 productId 와 요청 받은 상품 개수를 넣는다.
                CartItemAddRequest request = new CartItemAddRequest(productId, requestCount);

                CartProductResponse productResponse = new CartProductResponse(
                                productId, "네모바지", 10L, "판매자 1",
                                1L, "뉴발란스", 10000L, "PANTS", "square.png");

                // getCartProductInfo에 product를 넣었을 때, productResponse를 반환하도록 한다.
                given(catalogServiceClient.getCartProductInfo(productId)).willReturn(productResponse);

                // 새로운 mock 장바구니를 만들고 cartId를 10L 로 지정한다.
                Cart mockCart = Cart.builder().memberId(memberId).build();
                ReflectionTestUtils.setField(mockCart, "cartId", 10L);
                given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(mockCart));

                given(cartItemRepository.findByCartIdAndProductId(mockCart.getCartId(), productId))
                                .willReturn(Optional.empty());

                given(cartItemRepository.save(any(CartItem.class)))
                                .willAnswer(invocation -> {
                                        CartItem mockCartItem = invocation.getArgument(0);
                                        ReflectionTestUtils.setField(mockCartItem, "id", expectedCartItemId);
                                        return mockCartItem;
                                });

                // when
                Long resultId = cartService.addCartItemToCart(request, memberId);

                // then
                assertThat(resultId).isEqualTo(expectedCartItemId);

                // 실제로 repository.save가 1번 호출되었을 때
                verify(cartItemRepository, times(1)).save(any(CartItem.class));

        }

        @Test
        @DisplayName("장바구니 상품 넣기 성공 테스트 - 장바구니 안에 상품이 있을 경우")
        void success_addCartItemToCart_JustUpdateCount() {

                Long memberId = 1L;
                Long productId = 1L;
                Long cartId = 10L;
                Long existingItemId = 100L;
                Integer resentCount = 10;
                Integer addCount = 10;

                CartItemAddRequest request = new CartItemAddRequest(productId, addCount);

                CartProductResponse productResponse = new CartProductResponse(
                                productId, "네모바지", 10L, "판매자1",
                                1L, "뉴발란스", 10000L, "PANTS", "square.png");

                given(catalogServiceClient.getCartProductInfo(productId)).willReturn(productResponse);

                Cart mockCart = Cart.builder().memberId(memberId).build();
                ReflectionTestUtils.setField(mockCart, "cartId", cartId);

                given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(mockCart));

                CartItem existCartItem = CartItem.builder()
                                .cart(mockCart)
                                .productId(productId)
                                .cartCount(resentCount)
                                .build();
                ReflectionTestUtils.setField(existCartItem, "id", existingItemId);
                given(cartItemRepository.findByCartIdAndProductId(cartId, productId))
                                .willReturn(Optional.of(existCartItem));

                // when
                Long resultId = cartService.addCartItemToCart(request, memberId);

                // then
                // Id 가 동일한지 확인한다
                assertThat(resultId).isEqualTo(existingItemId);

                // 수량이 합산되었는지 확인
                assertThat(existCartItem.getCartCount()).isEqualTo(resentCount + addCount);
                verify(cartItemRepository, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("장바구니 조회 성공 테스트")
        void success_getCartList() {

                // given
                Long memberId = 4L;
                Long cartId = 12L;

                // 회원 아이디로 장바구니를 조회하는 로직을 테스트합니다.

                // memberId를 넣어 카트를 하나 생성한다.
                Cart cart = Cart.builder().memberId(memberId).build();

                // cartId를 넣어준다.
                ReflectionTestUtils.setField(cart, "cartId", cartId);
                log.info("생성된 장바구니 Id = {}", cartId);

                // CartProductResponseDto에 장바구니에 있는 상품의 정보를 넣어준다.
                // 상품 1
                CartProductResponse productResponse1 = new CartProductResponse(
                                1L, "네모바지", 10000L, "스펀지밥", 1L, "나이키", 10000L, "PANTS", "square.png");
                // 상품 2
                CartProductResponse productResponse2 = new CartProductResponse(
                                3L, "빨간티셔츠", 200000L, "뚱이", 2L, "에르메스", 20000L, "TOP", "redpants.png");

                // Catalog 서비스 클라이언트(Feign Client)에 productId를 넣고 productResponse를 뱉는다고 가정한다.
                given(catalogServiceClient.getCartProductInfo(1L)).willReturn(productResponse1);
                given(catalogServiceClient.getCartProductInfo(3L)).willReturn(productResponse2);

                // 장바구니 상품 1을 생성
                CartItem cartItem1 = CartItem.builder()
                                .cart(cart)
                                .productId(1L)
                                .cartCount(2)
                                .build();
                ReflectionTestUtils.setField(cartItem1, "id", 11L);

                // 장바구니 상품 2를 생성
                CartItem cartItem2 = CartItem.builder()
                                .cart(cart)
                                .productId(3L)
                                .cartCount(5)
                                .build();
                ReflectionTestUtils.setField(cartItem2, "id", 15L);

                cart.getCartItems().add(cartItem1);
                cart.getCartItems().add(cartItem2);

                // cartRepository가 memberId로 조회했을 때, cart를 반환한다고 설정
                given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));

                // when
                List<CartGetResponseDto> testList = cartService.getCartList(memberId);

                // then
                // 리스트 안의 상품이 2개인지 확인
                assertThat(testList).hasSize(2);

                // 병합 검증
                assertThat(testList)
                                .extracting("cartItemId", "productId", "productName", "productPrice", "cartCount",
                                                "storeName", "imageUrl")
                                .containsExactlyInAnyOrder(
                                                tuple(11L, 1L, "네모바지", 10000L, 2, "나이키", "square.png"),
                                                tuple(15L, 3L, "빨간티셔츠", 20000L, 5, "에르메스", "redpants.png"));

                verify(catalogServiceClient, times(1)).getCartProductInfo(1L);
                verify(catalogServiceClient, times(1)).getCartProductInfo(3L);
        }

        @Test
        @DisplayName("장바구니 수량 수정 성공 - 내 장바구니의 상품 수량을 정상적으로 변경한다.")
        void success_updateCart() {

                // given
                Long memberId = 4L;
                Long cartId = 12L;
                Long cartItemId = 13L;
                Integer oldCount = 2;
                Integer updateCount = 5;

                CartUpdateRequest request = new CartUpdateRequest(cartItemId, updateCount);

                Cart mockCart = Cart.builder().memberId(memberId).build();
                ReflectionTestUtils.setField(mockCart, "cartId", cartId);

                given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(mockCart));

                CartItem mockCartItem = CartItem.builder()
                                .cart(mockCart).productId(500L).cartCount(oldCount).build();
                ReflectionTestUtils.setField(mockCartItem, "id", cartItemId);

                given(cartItemRepository.findById(request.cartItemId())).willReturn(Optional.of(mockCartItem));

                // when
                cartService.updateCart(memberId, request);

                // then
                assertThat(mockCartItem.getCartCount()).isEqualTo(updateCount);

        }

        @Test
        @DisplayName("장바구니 수량 수정 실패 - 장바구니가 존재하지 않을 경우")
        void failed_updateCart_NotFoundCart() {
                Long memberId = 4L;
                CartUpdateRequest request = new CartUpdateRequest(1L, 4);

                // 장바구니가 존재하지 않을 경우
                given(cartRepository.findByMemberId(memberId)).willReturn(Optional.empty());
                log.info("장바구니가 존재하지 않습니다.");

                // when, Then
                assertThatThrownBy(() -> cartService.updateCart(memberId, request))
                                .isInstanceOf(CartException.class)
                                .extracting("errorCode")
                                .isEqualTo(CartErrorCode.CART_NOT_FOUND);

                log.info("장바구니 수량 수정 실패 테스트 완료 - 장바구니가 존재하지 않을 경우");
        }

        @Test
        @DisplayName("장바구니 수량 수정 실패 - 해당 상품이 존재하지 않을 경우")
        void failed_updateCart_NotFoundCartItem() {
                Long memberId = 1L;
                CartUpdateRequest request = new CartUpdateRequest(1L, 4);

                // Cart 는 존재
                Cart mockCart = Cart.builder().memberId(memberId).build();
                given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(mockCart));

                // CartItem이 존재하지 않을 경우
                given(cartItemRepository.findById(request.cartItemId())).willReturn(Optional.empty());

                // when, Then
                assertThatThrownBy(() -> cartService.updateCart(memberId, request))
                                .isInstanceOf(CartException.class)
                                .extracting("errorCode")
                                .isEqualTo(CartErrorCode.CART_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("내 장바구니에 있는 상품이 아님 - 권한이 없을 경우")
        void failed_updateCart_NotMyCartItem() {

                // Given
                Long memberId = 4L;
                Long cartId = 12L;
                Long otherCartId = 13L;

                CartUpdateRequest request = new CartUpdateRequest(1L, 4);

                Cart myCart = Cart.builder().memberId(memberId).build();
                ReflectionTestUtils.setField(myCart, "cartId", cartId);
                given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(myCart));

                // 다른 사람의 장바구니 객체 생성
                Cart otherCart = Cart.builder().memberId(100L).build();
                ReflectionTestUtils.setField(otherCart, "cartId", otherCartId);

                CartItem otherCartItem = CartItem.builder()
                                .cart(otherCart)
                                .productId(5L)
                                .cartCount(5)
                                .build();
                ReflectionTestUtils.setField(otherCartItem, "id", 15L);

                given(cartItemRepository.findById(request.cartItemId())).willReturn(Optional.of(otherCartItem));

                // when, then
                assertThatThrownBy(() -> cartService.updateCart(memberId, request))
                                .isInstanceOf(CartException.class)
                                .extracting("errorCode")
                                .isEqualTo(CartErrorCode.NOT_OWNER);
        }
}