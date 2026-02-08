package io.codebuddy.closetbuddy.domain.carts.repository;

import io.codebuddy.closetbuddy.domain.carts.model.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Cart 엔티티의 id 값인 cartId 필드와 연결해주기 위한 명시적인 JPQL 쿼리
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.productId = :productId")
    Optional<CartItem> findByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    // CartItemId를 통해 장바구니에 있는 장바구니 목록을 삭제하기 위한 JPQL 쿼리
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.memberId = :memberId AND ci.id IN :ids")
    void deleteCartItem(@Param("memberId") Long memberId, @Param("ids") List<Long> ids);

    // 회원 탈퇴 시 memberId를 통해 장바구니를 삭제합니다.
    void deleteCartByMemberId(Long memberId);
}
