package io.codebuddy.closetbuddy.domain.carts.repository;

import io.codebuddy.closetbuddy.domain.carts.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // ElseGet 메서드는 Optional 클래스에 있는 기능이기에 Optional을 붙인다.
    Optional<Cart> findByMemberId(Long memberId);
}
