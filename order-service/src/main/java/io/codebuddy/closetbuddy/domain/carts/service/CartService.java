package io.codebuddy.closetbuddy.domain.carts.service;

import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartItemAddRequest;
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
     * 장바구니를 생성합니다
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
        CartProductResponse product = catalogServiceClient.getProductWithCart(request.productId());

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

        if (cartRepository.existsByMemberId(memberId)) {
            Optional<Cart> findCart = cartRepository.findByMemberId(memberId);
            // CartItem의 객체를 CartGetResponseDto로 변환한다.
            return findCart.getCartItems()
                    .stream()
                    .map( variable -> new CartGetResponseDto(variable) )
                    .toList();
        } else{
            throw new CartException(CartErrorCode.CART_NOT_FOUND);
    }


    /**
     * 장바구니 수량을 수정합니다.
     * @param memberId
     * @param cartItemId
     * @param cartCount
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
     * @param memberId
     * @param cartItemId
     */
    @Transactional
    public void deleteCartItem(Long memberId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartException(CartErrorCode.CART_ITEM_NOT_FOUND));

        if (!cartItem.getCart().getMemberId().equals(memberId)) {
            throw new CartException(CartErrorCode.NOT_OWNER);
        }

        cartItemRepository.delete(cartItem);
    }
}
