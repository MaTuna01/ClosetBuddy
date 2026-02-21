package io.codebuddy.closetbuddy.domain.catalog.products.repository;

import feign.Param;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    //특정 store의 상품 목록 조회
    List<Product> findByStoreId(Long productId);

    //판매자가 가진 모든 가게의 상품을 조회하는 메서드
    List<Product> findAllByStoreId(Long storeId);

    // Order-Service 내 추천 시스템에서 사용되는 메서드
    // Query 문을 통해 여러 개의 productId를 받아 상품 정보를 한꺼번에 조회합니다.
    @Query("select p from Product p join fetch p.store s where p.productId in :productIds")
    List<Product> findRecommendProductWithStore(@Param("productIds") List<Long> productIds);
}