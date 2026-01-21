package io.codebuddy.closetbuddy.domain.sellers.service;

import io.codebuddy.closetbuddy.domain.sellers.model.dto.SellerResponse;
import io.codebuddy.closetbuddy.domain.sellers.model.dto.SellerUpsertRequest;
import io.codebuddy.closetbuddy.domain.sellers.model.entity.Seller;
import io.codebuddy.closetbuddy.domain.sellers.repository.SellerJpaRepository;
import io.codebuddy.closetbuddy.domain.stores.model.entity.Store;
import io.codebuddy.closetbuddy.domain.stores.repository.StoreJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerJpaRepository sellerJpaRepository;

    private final StoreJpaRepository storeJpaRepository;

    //판매자 등록(Create)
    @Transactional
    public Long registerSeller(Long loginMemberId, SellerUpsertRequest request) {
        if (sellerJpaRepository.existsByMemberId(loginMemberId)) {
            throw new IllegalStateException("이미 판매자로 등록된 사용자 입니다.");
        }

        Seller seller = Seller.builder()
                .memberId(loginMemberId)
                .sellerName(request.sellerName())
                .build();

        return sellerJpaRepository.saveAndFlush(seller).getSellerId();
    }
    //판매자 정보 조회 (Read)
    @Transactional(readOnly = true)
    public SellerResponse getSellerInfo(Long loginMemberId) {
        Seller seller = sellerJpaRepository.findByMemberId(loginMemberId)
                .orElseThrow(() -> new IllegalArgumentException("판매자가 아닌 사용자 입니다. 판매자 등록 후 이용해주세요."));

        return SellerResponse.from(seller);
    }

    //판매자 정보 수정(Update)
    @Transactional
    public  void updateSeller(Long loginMemberId, SellerUpsertRequest request) {
        //판매자 정보를 우선 불러오기
        Seller seller = sellerJpaRepository.findByMemberId(loginMemberId)
                .orElseThrow(() -> new IllegalArgumentException("판매자가 아닌 사용자 입니다. 판매자 등록 후 이용해주세요."));

        seller.update(request.sellerName());
    }

    //판매자 삭제(판매자 등록 해제, Delete)
    @Transactional
    public void unregisterSeller(Long loginMemberId) {
        Seller seller = sellerJpaRepository.findByMemberId(loginMemberId)
                .orElseThrow( () -> new IllegalArgumentException("판매자가 아닌 사용자 입니다."));

        List<Store> stores = storeJpaRepository.findAllBySeller(seller);
        storeJpaRepository.deleteAll(stores);
        sellerJpaRepository.delete(seller);
    }
}
