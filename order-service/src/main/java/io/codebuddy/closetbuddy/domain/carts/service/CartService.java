package io.codebuddy.closetbuddy.domain.carts.service;

import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartDeleteRequest;
import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartItemAddRequest;
import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartUpdateRequest;
import io.codebuddy.closetbuddy.domain.carts.model.dto.response.CartProductResponse;
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

        Cart cart = Cart.builder()
                .memberId(memberId)
                .build();

        Long cartId = cartRepository.saveAndFlush(cart).getCartId();
        return cartId;
    }

    @Transactional
    public Long addCartItemToCart(CartItemAddRequest request, Long memberId){
        // Feign 호출을 통해 상품 정보를 불러와 CartProductResponse에 저장
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
                    .productId(product.productId())
                    .productName(product.productName())
                    .productPrice(product.productPrice())
                    .storeName(product.storeName())
                    .cartCount(request.productCount())
                    .build();
            return cartItemRepository.save(cartItem).getId();
        }
    }


    /**
     * 회원 아이디로 장바구니를 조회합니다.
     * @param memberId
     * @return
     */
    public List<CartGetResponseDto> getCartList(Long memberId) {

        Cart findCart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

        // CartItem의 객체를 CartGetResponseDto로 변환한다.
        return findCart.getCartItems()
                .stream()
                .map( variable -> new CartGetResponseDto(variable) )
                .toList();
    }


    /**
     * 장바구니를 수정합니다.
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
     * 장바구니를 삭제합니다.
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
}
