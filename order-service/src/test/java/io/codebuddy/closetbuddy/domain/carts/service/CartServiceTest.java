package io.codebuddy.closetbuddy.domain.carts.service;

import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartItemAddRequest;
import io.codebuddy.closetbuddy.domain.carts.model.dto.response.CartGetResponseDto;
import io.codebuddy.closetbuddy.domain.carts.model.dto.response.CartProductResponse;
import io.codebuddy.closetbuddy.domain.carts.model.entity.Cart;
import io.codebuddy.closetbuddy.domain.carts.model.entity.CartItem;
import io.codebuddy.closetbuddy.domain.carts.repository.CartItemRepository;
import io.codebuddy.closetbuddy.domain.carts.repository.CartRepository;
import io.codebuddy.closetbuddy.domain.common.feign.CatalogServiceClient;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

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

        ReflectionTestUtils.setField(saveCart, "cartId", expectCartId);
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
        ReflectionTestUtils.setField(cart, "cartId", cartId);

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
        log.info("장바구니 상품 추가를 완료하였습니다.");
    }

    @Test
    @DisplayName("장바구니 상품 넣기 성공 테스트 - 장바구니 안에 상품이 있을 경우")
    void success_addCartItemToCart_JustUpdateCount() {

        // 장바구니에 상품이 있을 경우 수량만 추가해주는 로직 검증

        // given
        Long memberId = 5L;
        Long cartId = 10L;

        // CartItemRequest에 상품 Id와 상품 수량을 넣어준다.
        CartItemAddRequest request = new CartItemAddRequest(1L, 2);

        // CartProductResponseDto에 장바구니에 있는 상품의 정보를 넣어준다.
        CartProductResponse productResponse = new CartProductResponse(
                1L, "네모바지", 10000L, 100, "나이키");

        // Catalog 서비스 클라이언트(Feign Client)에 productId를 넣고 productResponse를 뱉는다고 가정한다.
        given(catalogServiceClient.getCartProductInfo(1L)).willReturn(productResponse);

        // 장바구니가 있다고 가정
        Cart cart = Cart.builder().memberId(memberId).build();
        ReflectionTestUtils.setField(cart, "cartId", cartId);
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));

        // 새로운 장바구니 객체를 생성한다. -> 장바구니에 상품이 존재한다는 가정
        CartItem existCart = CartItem.builder()
                .cart(cart)
                .productId(productResponse.productId())
                .productName(productResponse.productName())
                .productPrice(productResponse.productPrice())
                .storeName(productResponse.storeName())
                .cartCount(request.productCount())
                .build();

        // exitsCart에 cartId를 임의로 설정한다.
        ReflectionTestUtils.setField(existCart, "id", cartId);

        given(cartItemRepository.findByCartIdAndProductId(cart.getCartId(), request.productId()))
                .willReturn(Optional.of(existCart));

        log.info("장바구니 생성 및 장바구니 상품 존재");

        // when
        Long resultId = cartService.addCartItemToCart(request, memberId);

        // then
        assertThat(existCart.getId()).isEqualTo(resultId);
        log.info("존재하는 장바구니 Id와 실제로 만들어진 ID 일치");

        // save 메서드가 호출되지 않았는지 검증한다
        // 장바구니에 아이템이 있다는 가정하임으로


    }


    @Test
    @DisplayName("장바구니 조회 성공 테스트")
    void success_getCartList() {

        // given
        Long memberId = 4L;
        Long cartId = 102L;

        // 회원 아이디로 장바구니를 조회하는 로직을 테스트합니다.
        // 회원 아이디를 넣었을 때 올바른 카트 아이디를 반환하는지?
        // 회원 아이디로 조회한 Cart의 CartItem 값을 제대로 불러오는지

        // memberId를 넣어 카트를 하나 생성한다.
        Cart cart = Cart.builder().memberId(memberId).build();

        // cartId를 넣어준다.
        ReflectionTestUtils.setField(cart, "cartId", cartId);

        // 회원 아이디를 넣었을 때, cart 객체를 반환한다.
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));

        // CartProductResponseDto에 장바구니에 있는 상품의 정보를 넣어준다.
        // 상품 1
        CartProductResponse productResponse1 = new CartProductResponse(
                1L, "네모바지", 10000L, 100, "나이키");

        // 상품 2
        CartProductResponse productResponse2 = new CartProductResponse(
                3L, "빨간티셔츠", 200000L, 1000, "애르매스");

        // Catalog 서비스 클라이언트(Feign Client)에 productId를 넣고 productResponse를 뱉는다고 가정한다.
        given(catalogServiceClient.getCartProductInfo(1L)).willReturn(productResponse1);
        given(catalogServiceClient.getCartProductInfo(2L)).willReturn(productResponse2);

        // 상품 아이디가 1L인 상품을 1개 준비한다.
        CartItemAddRequest request1 = new CartItemAddRequest(1L, 1);

        // 상품 아이디가 3L인 상품을 3개 준비한다.
        CartItemAddRequest request2 = new  CartItemAddRequest(3L, 3);


        // request1 하나를 생성
        CartItem cartItem1 = CartItem.builder()
                .cart(cart)
                .productId(productResponse1.productId())
                .productName(productResponse1.productName())
                .productPrice(productResponse1.productPrice())
                .storeName(productResponse1.storeName())
                .cartCount(request1.productCount())
                .build();
        ReflectionTestUtils.setField(cartItem1, "id", 1L);

        // request2 하나를 생성
        CartItem cartItem2 = CartItem.builder()
                .cart(cart)
                .productId(productResponse2.productId())
                .productName(productResponse2.productName())
                .productPrice(productResponse2.productPrice())
                .storeName(productResponse2.storeName())
                .cartCount(request2.productCount())
                .build();
        ReflectionTestUtils.setField(cartItem2, "id", 1L);

        cart.getCartItems().add(cartItem1);
        cart.getCartItems().add(cartItem2);

        // cartRepository가 memberId로 조회했을 때, cart를 반환한다고 설정
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));

        // when
        List<CartGetResponseDto> testList = cartService.getCartList(memberId);

        // then
        assertThat(testList).hasSize(2);

        // testList에 작성되어있는 값이 실제 CartGetResponseDto와 같은지 검증
        assertThat(testList).extracting(CartGetResponseDto::cartItemId).containsExactlyInAnyOrder(cartItem1.getId(), cartItem2.getId());
        assertThat(testList).extracting(CartGetResponseDto::productId).containsExactlyInAnyOrder(cartItem1.getProductId(), cartItem2.getProductId());
        assertThat(testList).extracting(CartGetResponseDto::cartCount).containsExactlyInAnyOrder(cartItem1.getCartCount(), cartItem2.getCartCount());
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

}