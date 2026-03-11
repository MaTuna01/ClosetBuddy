package io.codebuddy.closetbuddy.domain.catalog.stores.service;


import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.sellers.exception.SellerErrorCode;
import io.codebuddy.closetbuddy.domain.catalog.sellers.exception.SellerException;
import io.codebuddy.closetbuddy.domain.catalog.sellers.model.entity.Seller;
import io.codebuddy.closetbuddy.domain.catalog.sellers.repository.SellerJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.stores.exception.StoreErrorCode;
import io.codebuddy.closetbuddy.domain.catalog.stores.exception.StoreException;
import io.codebuddy.closetbuddy.domain.catalog.stores.model.dto.StoreResponse;
import io.codebuddy.closetbuddy.domain.catalog.stores.model.dto.UpdateStoreRequest;
import io.codebuddy.closetbuddy.domain.catalog.stores.model.dto.UpsertStoreRequest;
import io.codebuddy.closetbuddy.domain.catalog.stores.model.entity.Store;
import io.codebuddy.closetbuddy.domain.catalog.stores.repository.StoreJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreJpaRepository storeJpaRepository;
    //상점에서 상품 제어를 위한 JpaRepository
    private final ProductJpaRepository productJpaRepository;
    private final SellerJpaRepository sellerJpaRepository;

    //상점 등록 서비스 로직
    public Long createStore(Long memberId, UpsertStoreRequest request) {

        //상점 등록을 할 수 있는 회원인지 점검
        Seller seller = sellerJpaRepository.findByMemberId(memberId)
                .orElseThrow( () -> new StoreException(StoreErrorCode.UNAUTHORIZED_ACCESS));

        Store store = request.toEntity(seller);

        return storeJpaRepository.save(store).getId();
    }

    //가게 정보 조회(단건) - 누구나 가능
    @Transactional(readOnly = true)
    public StoreResponse getStore(Long storeId) {
        Store store = storeJpaRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));
        return StoreResponse.from(store);
    }

    @Transactional
    //내 가게 정보 수정(상점 주인-판매자 만 가능)
    public StoreResponse updateStore(Long memberId, Long storeId, UpdateStoreRequest request) {
        Store store = storeJpaRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.NOT_OWNER));

        //상점 주인인지 검증
        validateOwner(memberId, store);

        store.update(request.storeName());
        return StoreResponse.from(store);
    }

    //내 상점 목록 조회(판매자용)
    @Transactional(readOnly = true)
    public List<StoreResponse> getMyStores(Long loginMemberId) {

        // 로그인한 회원의 Seller 정보 찾기
        Seller seller = sellerJpaRepository.findByMemberId(loginMemberId)
                .orElseThrow(() -> new SellerException(SellerErrorCode.SELLER_NOT_FOUND));

        // 해당 Seller가 가진 상점 리스트 조회
        List<Store> stores = storeJpaRepository.findAllBySeller(seller);

        // Entity List -> DTO List 변환 (Stream 활용)
        return stores.stream()
                .map(StoreResponse::from)
                .collect(Collectors.toList());
    }

    //전체 상점 목록 조회(공용)
    @Transactional(readOnly = true)
    public List<StoreResponse> getAllStores() {
        return storeJpaRepository.findAll().stream()
                .map(StoreResponse::from)
                .collect(Collectors.toList());
    }

    //내 가게 삭제
    @Transactional
    public void deleteStore(Long memberId, Long storeId) {
        Store store = storeJpaRepository.findById(storeId)
                .orElseThrow( () -> new StoreException(StoreErrorCode.NOT_OWNER));

        //삭제할 상점의 주인인지 검증
        validateOwner(memberId, store);

        storeJpaRepository.delete(store);
    }

    private void validateOwner(Long memberId, Store store) {
        // 현재 가게의 주인(Member) ID를 찾습니다.
        // Store -> Seller -> Member -> ID 순서로 타고 들어갑니다.
        Long ownerMemberId = store.getSeller().getMemberId();

        // 로그인한 사람(memberId)과 가게 주인(ownerMemberId)이 다르면 에러
        if (!ownerMemberId.equals(memberId)) {
            throw new StoreException(StoreErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

}
