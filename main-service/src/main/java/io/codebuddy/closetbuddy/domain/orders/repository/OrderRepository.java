package io.codebuddy.closetbuddy.domain.orders.repository;

import io.codebuddy.closetbuddy.domain.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByMemberId(Long memberId);
}
