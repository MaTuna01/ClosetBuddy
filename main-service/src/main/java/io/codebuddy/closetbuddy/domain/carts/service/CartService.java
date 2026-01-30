package io.codebuddy.closetbuddy.domain.carts.service;

import io.codebuddy.closetbuddy.domain.carts.exception.CartErrorCode;
import io.codebuddy.closetbuddy.domain.carts.exception.CartException;
import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartCreateRequestDto;
import io.codebuddy.closetbuddy.domain.carts.model.dto.response.CartGetResponseDto;
import io.codebuddy.closetbuddy.domain.carts.model.entity.Cart;
import io.codebuddy.closetbuddy.domain.carts.model.entity.CartItem;
import io.codebuddy.closetbuddy.domain.carts.repository.CartItemRepository;
import io.codebuddy.closetbuddy.domain.carts.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    /**
     * 회원가입 시에 장바구니를 생성합니다.
     * @return
     */
    @Transactional
    public Long createCart(Long memberId, CartCreateRequestDto request) {

        Cart cart = Cart.createCart(memberId);
        return cartRepository.save(cart).getId();
    }


    /**
     * 장바구니에 상품을 담습니다.
     */
    @Transactional
    public void addToCart(Long memberId, CartCreateRequestDto request) {
        // 회원의 장바구니 조회
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

        // 장바구니에 상품이 이미 존재하는지 확인
        Optional<CartItem> cartExist = cartItemRepository.findByMemberIdAndProductId(memberId, request.productId());

        // 장바구니에 이미 같은 상품이 존재한다면 수량만 변경할 수 있도록 한다.
        if(cartExist.isPresent()) {
            CartItem existItem =  cartExist.get();

            // 상품이 존재한다면 수량만 변경할 수 있도록
            existItem.updateCount(request.cartCount());

        } else {
            // 장바구니에 같은 상품이 존재하지 않는다면 장바구니에 상품을 추가할 수 있도록 한다.
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .productId(request.productId())
                    .cartCount(request.cartCount())
                    .build();

            cartItemRepository.save(cartItem);
        }
    }


    /**
     * 회원 아이디로 장바구니를 조회합니다.
     */
    public List<CartGetResponseDto> getCartList(Long memberId) {

        // 사용자의 장바구니를 조회합니다.
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

        // 장바구니의 전체 내역을 조회합니다.
        List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());

        return cartItems.stream()
                .map(CartGetResponseDto::new)
                .collect(Collectors.toList());

    }


    /**
     * 장바구니 수량을 수정합니다.
     */
    @Transactional
    public void updateCart(Long memberId, Long cartItemId, Integer cartCount) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartException(CartErrorCode.CART_ITEM_NOT_FOUND));

        if (!cartItem.getCart().getMemberId().equals(memberId)) {
            throw new CartException(CartErrorCode.NOT_OWNER);
        }

        cartItem.updateCount(cartCount);
    }


    /**
     * 장바구니 목록을 삭제합니다.
     */
    @Transactional
    public void deleteCartItem(Long memberId, Long cartItemId) {
        // 장바구니 상품이 존재하지 않을 때 예외처리
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartException(CartErrorCode.CART_ITEM_NOT_FOUND));

        // 장바구니의 주인이 아닐 때
        if (!cartItem.getCart().getMemberId().equals(memberId)) {
            throw new CartException(CartErrorCode.NOT_OWNER);
        }
        cartItemRepository.delete(cartItem);
    }
}
