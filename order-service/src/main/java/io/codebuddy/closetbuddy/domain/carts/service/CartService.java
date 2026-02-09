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

        // 장바구니가 이미 존재하면 409 CONFLICT 에러 발생
        if(cartRepository.existsByMemberId(memberId)) {
            throw new CartException(CartErrorCode.CART_ALREADY_EXITS);
        }

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
                    .sellerId(product.sellerId()) // 판매자 아이디
                    .sellerName(product.sellerName()) // 판매자 이름
                    .productId(request.productId()) // 상품 아이디
                    .productName(product.productName()) // 상품 이름
                    .productPrice(product.productPrice()) // 상품 가격
                    .storeId(product.storeId()) // 가게 아이디
                    .storeName(product.storeName()) // 가게 이름
                    .cartCount(request.productCount()) // 상품 개수
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


    /**
     * 회원 탈퇴 시 사용하는 삭제 메서드
     * @param memberId
     */
    @Transactional
    public void deleteCart(Long memberId) {
        cartItemRepository.deleteByCart_MemberId(memberId);
        cartRepository.deleteById(memberId);
    }
}
