package io.codebuddy.closetbuddy.domain.sellers.repository;

import io.codebuddy.closetbuddy.domain.sellers.model.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerJpaRepository extends JpaRepository<Seller, Long> {

    //이미 판매자로 등록된 멤버인지 검사
    boolean existsByMemberId(Long memberId);

    Optional<Seller> findByMemberId(Long memberId);
}
