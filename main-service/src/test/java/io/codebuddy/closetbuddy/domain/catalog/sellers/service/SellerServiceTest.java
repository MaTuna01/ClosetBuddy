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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.codebuddy.closetbuddy.domain.catalog.sellers.exception.SellerErrorCode.ROLE_GRANT_FAILED;
import static io.codebuddy.closetbuddy.domain.catalog.sellers.exception.SellerErrorCode.ROLE_REVOKE_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SellerService 단위 테스트")
class SellerServiceTest {

    @InjectMocks
    private SellerService sellerService;

    @Mock
    private SellerJpaRepository sellerJpaRepository;

    @Mock
    private StoreJpaRepository storeJpaRepository;

    @Mock
    private UserServiceClient userServiceClient;

    // 테스트용 헬퍼 셀러
    private Seller createSeller(Long sellerId, Long memberId, String sellerName) {
        return Seller.builder()
                .sellerId(sellerId)
                .memberId(memberId)
                .sellerName(sellerName)
                .build();
    }

    // 판매자 등록
    @Nested
    @DisplayName("판매자 등록 (registerSeller)")
    class RegisterSeller {

        @Test
        @DisplayName("정상 등록 시 판매자 ID 반환 및 Feign grantSellerRole 호출")
        void registerSeller_success() {

            //테스트 멤버(판매자)
            Long memberId = 1L;
            SellerUpsertRequest request = new SellerUpsertRequest("테스트 판매자");
            Seller savedSeller = createSeller(100L, memberId, "테스트 판매자");

            when(sellerJpaRepository.findByMemberId(memberId)).thenReturn(Optional.empty());
            when(sellerJpaRepository.existsBySellerName("테스트 판매자")).thenReturn(false);
            when(sellerJpaRepository.saveAndFlush(any(Seller.class))).thenReturn(savedSeller);
            when(userServiceClient.grantSellerRole(memberId)).thenReturn(ResponseEntity.ok().build());

            Long sellerId = sellerService.registerSeller(memberId, request);

            assertThat(sellerId).isEqualTo(100L);
            verify(sellerJpaRepository).saveAndFlush(any(Seller.class));
            verify(userServiceClient).grantSellerRole(memberId);
        }

        // 이미 등록퇸 판매자 테스트
        @Test
        @DisplayName("이미 등록된 판매자면 ALREADY_REGISTERED 예외")
        void registerSeller_ALREADY_REGISTERED_exception() {

            Long memberId = 1L;
            SellerUpsertRequest request = new SellerUpsertRequest("테스트 판매자");
            Seller existingSeller = createSeller(100L, memberId, "기존 판매자");

            when(sellerJpaRepository.findByMemberId(memberId)).thenReturn(Optional.of(existingSeller));

            assertThatThrownBy(() -> sellerService.registerSeller(memberId, request))
                    .isInstanceOf(SellerException.class)
                    .satisfies(ex -> assertThat(((SellerException) ex).getErrorCode())
                            .isEqualTo(SellerErrorCode.ALREADY_REGISTERED));

            verify(sellerJpaRepository, never()).saveAndFlush(any());
            verify(userServiceClient, never()).grantSellerRole(anyLong());
        }

        // 판매자 이름 중복 예외 테스트
        @Test
        @DisplayName("판매자 이름 중복 시 SELLER_NAME_DUPLICATED 예외")
        void registerSeller_SELLER_NAME_DUPLICATED_exception() {
            Long memberId = 1L;
            SellerUpsertRequest request = new SellerUpsertRequest("중복이름");

            when(sellerJpaRepository.findByMemberId(memberId)).thenReturn(Optional.empty());
            when(sellerJpaRepository.existsBySellerName("중복이름")).thenReturn(true);

            assertThatThrownBy(() -> sellerService.registerSeller(memberId, request))
                    .isInstanceOf(SellerException.class)
                    .satisfies(ex -> assertThat(((SellerException) ex).getErrorCode())
                            .isEqualTo(SellerErrorCode.SELLER_NAME_DUPLICATED));

            verify(sellerJpaRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Feign 호출 실패 시 ROLE_GRANT_FAILED 예외")
        void registerSeller_Feign_ROLE_GRANT_FAILED_exception() {
            Long memberId = 1L;
            SellerUpsertRequest request = new SellerUpsertRequest("테스트 판매자");
            Seller savedSeller = createSeller(100L, memberId, "테스트 판매자");

            when(sellerJpaRepository.findByMemberId(memberId)).thenReturn(Optional.empty());
            when(sellerJpaRepository.existsBySellerName("테스트 판매자")).thenReturn(false);
            when(sellerJpaRepository.saveAndFlush(any(Seller.class))).thenReturn(savedSeller);
            when(userServiceClient.grantSellerRole(memberId)).thenThrow(new SellerException(ROLE_GRANT_FAILED));

            assertThatThrownBy(() -> sellerService.registerSeller(memberId, request))
                    .isInstanceOf(SellerException.class)
                    .satisfies(ex -> assertThat(((SellerException) ex).getErrorCode())
                            .isEqualTo(SellerErrorCode.ROLE_GRANT_FAILED));
        }
    }

    // 판매자 조회
    @Nested
    @DisplayName("판매자 조회 (getSellerInfo)")
    class GetSellerInfo {

        @Test
        @DisplayName("정상 조회 시 SellerResponse 반환")
        void getSellerInfo_success() {

            Long memberId = 1L;
            Seller seller = createSeller(100L, memberId, "테스트 판매자");
            when(sellerJpaRepository.findByMemberId(memberId)).thenReturn(Optional.of(seller));

            SellerResponse response = sellerService.getSellerInfo(memberId);

            assertThat(response.sellerId()).isEqualTo(100L);
            assertThat(response.sellerName()).isEqualTo("테스트 판매자");
        }

        @Test
        @DisplayName("미등록 판매자 조회 시 SELLER_NOT_FOUND 예외")
        void getSellerInfo_SELLER_NOT_FOUND_exception() {
            Long memberId = 999L;
            when(sellerJpaRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sellerService.getSellerInfo(memberId))
                    .isInstanceOf(SellerException.class)
                    .satisfies(ex -> assertThat(((SellerException) ex).getErrorCode())
                            .isEqualTo(SellerErrorCode.SELLER_NOT_FOUND));
        }
    }

    // 판매자 수정
    @Nested
    @DisplayName("판매자 수정 (updateSeller)")
    class UpdateSeller {

        @Test
        @DisplayName("정상 수정 시 이름 변경")
        void updateSeller_success() {

            Long memberId = 1L;
            SellerUpsertRequest request = new SellerUpsertRequest("새이름");
            Seller seller = createSeller(100L, memberId, "기존이름");

            when(sellerJpaRepository.findByMemberId(memberId)).thenReturn(Optional.of(seller));
            when(sellerJpaRepository.existsBySellerNameAndSellerIdNot("새이름", 100L)).thenReturn(false);

            sellerService.updateSeller(memberId, request);

            assertThat(seller.getSellerName()).isEqualTo("새이름");
        }

        @Test
        @DisplayName("자기 제외 이름 중복 시 SELLER_NAME_DUPLICATED 예외")
        void updateSeller_SELLER_NAME_DUPLICATED_exception() {

            Long memberId = 1L;
            SellerUpsertRequest request = new SellerUpsertRequest("중복이름");
            Seller seller = createSeller(100L, memberId, "기존이름");

            when(sellerJpaRepository.findByMemberId(memberId)).thenReturn(Optional.of(seller));
            when(sellerJpaRepository.existsBySellerNameAndSellerIdNot("중복이름", 100L)).thenReturn(true);

            assertThatThrownBy(() -> sellerService.updateSeller(memberId, request))
                    .isInstanceOf(SellerException.class)
                    .satisfies(ex -> assertThat(((SellerException) ex).getErrorCode())
                            .isEqualTo(SellerErrorCode.SELLER_NAME_DUPLICATED));
        }

        @Test
        @DisplayName("미등록 판매자 수정 시 UNAUTHORIZED_ACCESS 예외")
        void updateSeller_미등록판매자_예외() {

            Long memberId = 999L;
            SellerUpsertRequest request = new SellerUpsertRequest("새이름");

            when(sellerJpaRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sellerService.updateSeller(memberId, request))
                    .isInstanceOf(SellerException.class)
                    .satisfies(ex -> assertThat(((SellerException) ex).getErrorCode())
                            .isEqualTo(SellerErrorCode.UNAUTHORIZED_ACCESS));
        }
    }

    // 판매자 삭제
    @Nested
    @DisplayName("판매자 삭제 (unregisterSeller)")
    class UnregisterSeller {

        @Test
        @DisplayName("정상 삭제 시 스토어/판매자 삭제 및 Feign revokeSellerRole 호출")
        void unregisterSeller_success() {

            Long memberId = 1L;
            Seller seller = createSeller(100L, memberId, "테스트 판매자");
            List<Store> stores = List.of(
                    Store.builder().id(1L).seller(seller).storeName("상점1").build(),
                    Store.builder().id(2L).seller(seller).storeName("상점2").build());

            when(sellerJpaRepository.findByMemberId(memberId)).thenReturn(Optional.of(seller));
            when(storeJpaRepository.findAllBySeller(seller)).thenReturn(stores);
            when(userServiceClient.revokeSellerRole(memberId)).thenReturn(ResponseEntity.ok().build());

            sellerService.unregisterSeller(memberId);

            verify(storeJpaRepository).deleteAll(stores);
            verify(sellerJpaRepository).delete(seller);
            verify(userServiceClient).revokeSellerRole(memberId);
        }

        @Test
        @DisplayName("미등록 판매자 삭제 시 UNAUTHORIZED_ACCESS 예외")
        void unregisterSeller_UNAUTHORIZED_ACCESS_exception() {

            Long memberId = 999L;
            when(sellerJpaRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sellerService.unregisterSeller(memberId))
                    .isInstanceOf(SellerException.class)
                    .satisfies(ex -> assertThat(((SellerException) ex).getErrorCode())
                            .isEqualTo(SellerErrorCode.UNAUTHORIZED_ACCESS));
        }

        @Test
        @DisplayName("Feign 역할 해제 실패 시 ROLE_REVOKE_FAILED 예외")
        void unregisterSeller_Feign_ROLE_REVOKE_FAILED_exception() {

            Long memberId = 1L;
            Seller seller = createSeller(100L, memberId, "테스트 판매자");

            when(sellerJpaRepository.findByMemberId(memberId)).thenReturn(Optional.of(seller));
            when(storeJpaRepository.findAllBySeller(seller)).thenReturn(Collections.emptyList());
            when(userServiceClient.revokeSellerRole(memberId)).thenThrow(new SellerException(ROLE_REVOKE_FAILED));

            assertThatThrownBy(() -> sellerService.unregisterSeller(memberId))
                    .isInstanceOf(SellerException.class)
                    .satisfies(ex -> assertThat(((SellerException) ex).getErrorCode())
                            .isEqualTo(ROLE_REVOKE_FAILED));
        }
    }
}
