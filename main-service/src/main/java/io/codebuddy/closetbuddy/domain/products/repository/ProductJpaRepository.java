package io.codebuddy.closetbuddy.domain.products.repository;

import io.codebuddy.closetbuddy.domain.products.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    //특정 store의 상품 목록 조회
    List<Product> findByStoreId(Long productId);

    //판매자가 가진 모든 가게의 상품을 조회하는 메서드
    List<Product> findAllByStoreId(Long storeId);
}