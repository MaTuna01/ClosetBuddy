package io.codebuddy.closetbuddy.domain.payments.repository;

import io.codebuddy.closetbuddy.domain.payments.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    boolean existsByOrderId(Long orderId);

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
}
