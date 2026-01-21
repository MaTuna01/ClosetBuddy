package io.codebuddy.closetbuddy.domain.stores.repository;

import io.codebuddy.closetbuddy.domain.sellers.model.entity.Seller;
import io.codebuddy.closetbuddy.domain.stores.model.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreJpaRepository extends JpaRepository<Store, Long> {

    //특정 판매자의 모든 상점 조회(list로 반환)
    List<Store> findAllBySeller(Seller seller);
}
