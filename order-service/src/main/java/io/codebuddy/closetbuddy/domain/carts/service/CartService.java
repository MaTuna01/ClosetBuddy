package io.codebuddy.closetbuddy.domain.carts.service;

import feign.FeignException;
import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartDeleteRequest;
import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartItemAddRequest;
import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartUpdateRequest;
import io.codebuddy.closetbuddy.domain.common.feign.dto.CartProductResponse;
import io.codebuddy.closetbuddy.domain.common.feign.CatalogServiceClient;
import lombok.RequiredArgsConstructor;
import io.codebuddy.closetbuddy.domain.carts.exception.CartErrorCode;
import io.codebuddy.closetbuddy.domain.carts.exception.CartException;
import io.codebuddy.closetbuddy.domain.carts.model.dto.response.CachedCartItem;
import io.codebuddy.closetbuddy.domain.carts.model.dto.response.CartGetResponseDto;
import io.codebuddy.closetbuddy.domain.carts.model.entity.Cart;
import io.codebuddy.closetbuddy.domain.carts.model.entity.CartItem;
import io.codebuddy.closetbuddy.domain.carts.repository.CartItemRepository;
import io.codebuddy.closetbuddy.domain.carts.repository.CartRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

        return cartRepository.saveAndFlush(cart).getCartId();
    }

    @CacheEvict(value = "cart", key = "#memberId")
    @Transactional
    public Long addCartItemToCart(CartItemAddRequest request, Long memberId) {

        try {
            catalogServiceClient.getCartProductInfo(request.productId());
        } catch (FeignException e) {
            throw new CartException(CartErrorCode.PRODUCT_NOT_FOUND);
        }

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().memberId(memberId).build()));

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getCartId(), request.productId());

        if (existingItem.isPresent()) {
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
     * 장바구니 구조만 Redis에 캐싱
     * 상품은 포함하지 않음
     *
     * @param memberId
     * @return
     */
    @Cacheable(value = "cart", key = "#memberId")
    @Transactional(readOnly = true)
    public List<CachedCartItem> getCachedCartItems(Long memberId) {

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

        return cart.getCartItems().stream()
                .map(item -> new CachedCartItem(
                        item.getId(),
                        item.getProductId(),
                        item.getCartCount()))
                .toList();
    }

    /**
     * 회원 아이디로 장바구니를 조회합니다.
     * 장바구니 안의 내용은 캐싱
     * 상품의 가격, 이름, 이미지는 항상 catalog-service에서 최신 정보 반환
     *
     * @param memberId
     * @return
     */
    public List<CartGetResponseDto> getCartList(Long memberId) {

        // 장바구니 구조는 캐시에서 조회
        List<CachedCartItem> cachedItems = getCachedCartItems(memberId);

        if (cachedItems.isEmpty()) {
            return List.of();
        }

        List<CartGetResponseDto> cartGetResponseDto = new ArrayList<>();

        for (CachedCartItem cachedItem : cachedItems) {
            // 상품는 캐싱 x -> 최신 값 가져와야하기때문에
            CartProductResponse product = catalogServiceClient.getCartProductInfo(cachedItem.productId());

            cartGetResponseDto.add(new CartGetResponseDto(
                    cachedItem.cartItemId(), // 장바구니 상품 아이디
                    cachedItem.productId(), // 상품 아이디
                    product.productName(), // 상품 이름
                    product.productPrice(), // 상품당 가격
                    cachedItem.count(), // 장바구니에 담긴 상품 수량
                    product.storeName(), // 상점 이름
                    product.imageUrl() // 이미지
            ));
        }
        return cartGetResponseDto;
    }

    /**
     * 장바구니를 수량을 수정합니다.
     *
     * @param memberId
     * @param request
     */
    @CacheEvict(value = "cart", key = "#memberId")
    @Transactional
    public void updateCart(Long memberId, CartUpdateRequest request) {
        // 회원의 장바구니가 존재하는지 확인
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

        // cartItem 객체에 cartItemId가 존재하는지 확인 -> 수정할 cartItem 상품이 있는지 확인
        CartItem cartItem = cartItemRepository.findById(request.cartItemId())
                .orElseThrow(() -> new CartException(CartErrorCode.CART_ITEM_NOT_FOUND));

        // cartId와 cartItem이 가지고 있는 cartId가 같은지 확인
        if (!cart.getCartId().equals(cartItem.getCart().getCartId())) {
            throw new CartException(CartErrorCode.NOT_OWNER);
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
    @CacheEvict(value = "cart", key = "#memberId")
    @Transactional
    public void deleteCartItem(Long memberId, CartDeleteRequest request) {

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

        // 장바구니 상품 리스트가 비어있을 경우, 예외를 반환한다.
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new CartException(CartErrorCode.CART_ITEM_NOT_FOUND);
        }
        cartItemRepository.deleteCartItem(memberId, request.cartItemList());
    }

    /**
     * 회원 탈퇴 시 사용하는 삭제 메서드
     *
     * @param memberId
     */
    @CacheEvict(value = "cart", key = "#memberId")
    @Transactional
    public void deleteCart(Long memberId) {
        cartRepository.deleteById(memberId);
    }
}
