package io.codebuddy.closetbuddy.domain.carts.repository;

import io.codebuddy.closetbuddy.domain.carts.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
