package io.codebuddy.closetbuddy.domain.catalog.sellers.service;

import io.codebuddy.closetbuddy.domain.catalog.sellers.exception.SellerErrorCode;
import io.codebuddy.closetbuddy.domain.catalog.sellers.exception.SellerException;
import io.codebuddy.closetbuddy.domain.catalog.sellers.model.dto.SellerResponse;
import io.codebuddy.closetbuddy.domain.catalog.sellers.model.dto.SellerUpsertRequest;
import io.codebuddy.closetbuddy.domain.catalog.sellers.model.entity.Seller;
import io.codebuddy.closetbuddy.domain.catalog.sellers.repository.SellerJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.stores.model.entity.Store;
import io.codebuddy.closetbuddy.domain.catalog.stores.repository.StoreJpaRepository;
import io.codebuddy.closetbuddy.domain.common.feign.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerJpaRepository sellerJpaRepository;

    private final StoreJpaRepository storeJpaRepository;
    private final UserServiceClient userServiceClient;

    // 판매자 등록(Create)
    @Transactional
    public Long registerSeller(Long loginMemberId, SellerUpsertRequest request) {
        sellerJpaRepository.findByMemberId(loginMemberId)
                .ifPresent(seller -> {
                    throw new SellerException(SellerErrorCode.ALREADY_REGISTERED);
                });
        // seller Entity 생성
        Seller seller = Seller.builder()
                .memberId(loginMemberId)
                .sellerName(request.sellerName())
                .build();

        Long sellerId = sellerJpaRepository.saveAndFlush(seller).getSellerId();

        // user-service에 판매자 역할 부여 요청(동기 요청)
        try {
            log.info("user-service에 판매자 역할 부여 요청 - memberId: {}", loginMemberId);
            userServiceClient.grantSellerRole();
            log.info("user-service 판매자 역할 부여 완료 - memberId: {}", loginMemberId);
        } catch (SellerException e) {
            log.error("user-service 판매자 역할 부여 실패 - memberId: {}, errorCode: {}", loginMemberId, e.getErrorCode());
            throw e;
        } catch (Exception e) {
            log.error("user-service 판매자 역할 부여 실패 - memberId: {}, error: {}",  loginMemberId, e.getMessage());
            throw new SellerException(SellerErrorCode.ROLE_GRANT_FAILED);
        }

        return sellerId;
    }

    // 판매자 정보 조회 (Read)
    @Transactional(readOnly = true)
    public SellerResponse getSellerInfo(Long loginMemberId) {
        Seller seller = sellerJpaRepository.findByMemberId(loginMemberId)
                .orElseThrow(() -> new SellerException(SellerErrorCode.SELLER_NOT_FOUND));

        return SellerResponse.from(seller);
    }

    // 판매자 정보 수정(Update)
    @Transactional
    public  void updateSeller(Long loginMemberId, SellerUpsertRequest request) {
        // 판매자 정보를 우선 불러오기
        Seller seller = sellerJpaRepository.findByMemberId(loginMemberId)
                .orElseThrow(() -> new SellerException(SellerErrorCode.UNAUTHORIZED_ACCESS));

        seller.update(request.sellerName());
    }

    // 판매자 삭제(판매자 등록 해제, Delete)
    @Transactional
    public void unregisterSeller(Long loginMemberId) {
        Seller seller = sellerJpaRepository.findByMemberId(loginMemberId)
                .orElseThrow(() -> new SellerException(SellerErrorCode.UNAUTHORIZED_ACCESS));

        // 1. 스토어 및 판매자 삭제
        List<Store> stores = storeJpaRepository.findAllBySeller(seller);
        storeJpaRepository.deleteAll(stores);
        sellerJpaRepository.delete(seller);

        // 2. user-service에 판매자 역할 해제 요청 (동기 호출)
        try {
            log.info("user-service에 판매자 역할 해제 요청 - memberId: {}", loginMemberId);
            userServiceClient.revokeSellerRole();
            log.info("user-service 판매자 역할 해제 완료 - memberId: {}", loginMemberId);
        } catch (SellerException e) {
            // FeignErrorDecoder에서 변환된 SellerException은 그대로 전파
            log.error("user-service 판매자 역할 해제 실패 - memberId: {}, errorCode: {}", loginMemberId, e.getErrorCode());
            throw e;
        } catch (Exception e) {
            log.error("user-service 판매자 역할 해제 실패 - memberId: {}, error: {}", loginMemberId, e.getMessage());
            throw new SellerException(SellerErrorCode.ROLE_REVOKE_FAILED);
        }
    }
}
