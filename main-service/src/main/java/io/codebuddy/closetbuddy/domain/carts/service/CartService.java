package io.codebuddy.closetbuddy.domain.carts.service;

import io.codebuddy.closetbuddy.domain.common.repository.MemberRepository;
import io.codebuddy.closetbuddy.domain.carts.dto.request.CartCreateRequestDto;
import io.codebuddy.closetbuddy.domain.carts.dto.response.CartGetResponseDto;
import io.codebuddy.closetbuddy.domain.carts.entity.Cart;
import io.codebuddy.closetbuddy.domain.carts.entity.CartItem;
import io.codebuddy.closetbuddy.domain.carts.repository.CartItemRepository;
import io.codebuddy.closetbuddy.domain.carts.repository.CartRepository;

import io.codebuddy.closetbuddy.domain.common.model.entity.Member;
import io.codebuddy.closetbuddy.domain.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.products.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ProductJpaRepository productJpaRepository;


    /**
     * 장바구니가 존재하는지 판단하고 없으면 장바구니 리스트를 만듭니다.
     *
     * @param memberId
     * @param request
     * @return
     */
    @Transactional
    public Long createCart(Long memberId, CartCreateRequestDto request) {

        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));

        // 2. 상품 조회
        Product product = productJpaRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 없습니다."));

        // 장바구니 조회 없으면 생성
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    Cart newCart = Cart.createCart(member);
                    return cartRepository.save(newCart);
                });

        // 장바구니 상품에 삼품과 상품 개수를 담습니다.
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .cartCount(request.cartCount())
                .build();

        cartItemRepository.save(cartItem);

        // 아이디 반환
        return cartItem.getId();
    }


    /**
     * 회원 아이디로 장바구니를 조회합니다.
     *
     * @param memberId
     * @return
     */
    public List<CartGetResponseDto> getCartList(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId).orElse(null);

        if (cart == null) {
            return new ArrayList<>();
        }

        return cart.getCartItems().stream()
                .map(CartGetResponseDto::new)
                .collect(Collectors.toList());
    }


    /**
     * 장바구니를 수정합니다. (수량)
     *
     * @param memberId
     * @param cartItemId
     * @param cartCount
     */
    @Transactional
    public void updateCart(Long memberId, Long cartItemId, Integer cartCount) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException(""));

        if (!cartItem.getCart().getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        cartItem.updateCount(cartCount);
    }


    /**
     * 장바구니 목록을 삭제합니다.
     *
     * @param memberId
     * @param cartItemId
     */
    @Transactional
    public void deleteCartItem(Long memberId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니에 없는 상품입니다."));

        if (!cartItem.getCart().getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        cartItemRepository.delete(cartItem);
    }
}
