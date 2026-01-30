package io.codebuddy.closetbuddy.domain.carts.repository;

import io.codebuddy.closetbuddy.domain.carts.model.entity.Cart;
import io.codebuddy.closetbuddy.domain.carts.model.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByMemberIdAndProductId(Long memberId, Long productId);

    List<CartItem> findAllByCartId(Long id);
}
