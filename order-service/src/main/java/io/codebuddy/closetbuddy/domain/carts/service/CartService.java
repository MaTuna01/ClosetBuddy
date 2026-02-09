package io.codebuddy.closetbuddy.domain.carts.service;

import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartDeleteRequest;
import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartItemAddRequest;
import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartUpdateRequest;
import io.codebuddy.closetbuddy.domain.common.feign.dto.CartProductResponse;
import io.codebuddy.closetbuddy.domain.common.feign.CatalogServiceClient;
import lombok.RequiredArgsConstructor;
import io.codebuddy.closetbuddy.domain.carts.exception.CartErrorCode;
import io.codebuddy.closetbuddy.domain.carts.exception.CartException;
import io.codebuddy.closetbuddy.domain.carts.model.dto.response.CartGetResponseDto;
import io.codebuddy.closetbuddy.domain.carts.model.entity.Cart;
import io.codebuddy.closetbuddy.domain.carts.model.entity.CartItem;
import io.codebuddy.closetbuddy.domain.carts.repository.CartItemRepository;
import io.codebuddy.closetbuddy.domain.carts.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CatalogServiceClient catalogServiceClient;

    /**
     * 회원가입시 장바구니를 생성합니다
     *
     * @param memberId
     * @return
     */
    @Transactional
    public Long createCart(Long memberId) {

        Cart cart = Cart.builder().memberId(memberId).build();

        Long cartId = cartRepository.saveAndFlush(cart).getCartId();

        return cartId;
    }

    @Transactional
    public Long addCartItemToCart(CartItemAddRequest request, Long memberId){
        // Feign 호출을 통한 최신 정보 조회
        CartProductResponse product = catalogServiceClient.getCartProductInfo(request.productId());

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().memberId(memberId).build()
                ));

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getCartId(), request.productId());

        if(existingItem.isPresent()){
            // 이미 있으면 수량만 추가
            existingItem.get().addCount(request.productCount());
            return existingItem.get().getId();

        } else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .productId(request.productId())
                    .cartCount(request.productCount())
                    .build();
            return cartItemRepository.save(cartItem).getId();
        }
    }


    /**
     * 회원 아이디로 장바구니를 조회합니다.
     *
     * @param memberId
     * @return
     */
    public List<CartGetResponseDto> getCartList(Long memberId) {

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

        List<CartItem> cartItems = cart.getCartItems();
        List<CartGetResponseDto> cartGetResponseDto = new ArrayList<>();

        for(CartItem cartItem : cartItems) {
            // 외부 API 호출을 통해 장바구니가 최신 상품 정보를 반영하도록 합니다.
            CartProductResponse product = catalogServiceClient.getCartProductInfo(cartItem.getProductId());

            // DB에 있는 수량과 외부에서 가져온 상품 정보를 합쳐 Dto를 반환합니다.
            cartGetResponseDto.add(new CartGetResponseDto(
                    cartItem.getId(), // 장바구니 상품 아이디
                    cartItem.getProductId(), // 상품 아이디
                    product.productName(), // 상품 이름
                    product.productPrice(), // 상품당 가격
                    cartItem.getCartCount(), // 장바구니에 담긴 상품 수량
                    product.storeName(), // 상점 이름
                    product.imageUrl() // 이미지
            ));
        }
        return cartGetResponseDto;
    }


    /**
     * 장바구니를 수정합니다.
     *
     * @param memberId
     * @param request
     */
    @Transactional
    public void updateCart(Long memberId, CartUpdateRequest request) {
        // 회원의 장바구니가 존재하는지 확인
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

        // cartItem 객체에 cartItemId가 존재하는지 확인 -> 수정할 cartItem 상품이 있는지 확인
        CartItem cartItem = cartItemRepository.findById(request.cartItemId())
                .orElseThrow(() -> new CartException(CartErrorCode.CART_ITEM_NOT_FOUND));

        // cartId와 cartItem이 가지고 있는 cartId가 같은지 확인
        if(!cart.getCartId().equals(cartItem.getCart().getCartId())) {
            throw new CartException(CartErrorCode.CART_ITEM_NOT_FOUND);
        }

        // requestDto 를 받아와서 장바구니 수량을 수정합니다.
        cartItem.updateCount(request.cartCount());
    }


    /**
     * 장바구니 상품을 삭제합니다.
     *
     * @param memberId
     * @param request
     */
    @Transactional
    public void deleteCartItem(Long memberId, CartDeleteRequest request) {
        // 장바구니 상품 리스트가 비어있을 경우, 예외를 반환한다.
        if(request.cartItemList() == null || request.cartItemList().isEmpty()){
            throw new CartException(CartErrorCode.CART_ITEM_NOT_FOUND);
        }
        cartItemRepository.deleteCartItem(memberId, request.cartItemList());
    }


    /**
     * 회원 탈퇴 시 사용하는 삭제 메서드
     *
     * @param memberId
     */
    @Transactional
    public void deleteCart(Long memberId) {
        cartRepository.deleteById(memberId);
    }
}
