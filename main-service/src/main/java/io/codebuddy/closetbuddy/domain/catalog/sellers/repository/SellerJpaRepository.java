package io.codebuddy.closetbuddy.domain.catalog.sellers.repository;

import io.codebuddy.closetbuddy.domain.catalog.sellers.model.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerJpaRepository extends JpaRepository<Seller, Long> {

    //이미 판매자로 등록된 멤버인지 검사
    boolean existsByMemberId(Long memberId);

    // 판매자 이름 중복 체크
    boolean existsBySellerName(String sellerName);

    // 판매자 이름 중복 체크 (수정 시 - 자기 자신 제외)
    boolean existsBySellerNameAndSellerIdNot(String sellerName, Long sellerId);

    Optional<Seller> findByMemberId(Long memberId);
}
