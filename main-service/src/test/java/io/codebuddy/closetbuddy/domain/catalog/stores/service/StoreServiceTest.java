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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreService 단위 테스트")
class StoreServiceTest {

    @InjectMocks
    private StoreService storeService;

    @Mock
    private StoreJpaRepository storeJpaRepository;

    @Mock
    private ProductJpaRepository productJpaRepository;

    @Mock
    private SellerJpaRepository sellerJpaRepository;

    // 테스트용 헬퍼
    private Seller createSeller(Long sellerId, Long memberId, String sellerName) {
        return Seller.builder()
                .sellerId(sellerId)
                .memberId(memberId)
                .sellerName(sellerName)
                .build();
    }

    private Store createStore(Long storeId, Seller seller, String storeName) {
        return Store.builder()
                .id(storeId)
                .seller(seller)
                .storeName(storeName)
                .build();
    }

    // 상점 등록
    @Nested
    @DisplayName("상점 등록 (createStore)")
    class CreateStore {

        @Test
        @DisplayName("정상 등록 시 상점 ID 반환")
        void createStore_success() {

            Long memberId = 1L;
            UpsertStoreRequest request = new UpsertStoreRequest("테스트 상점");
            Seller seller = createSeller(100L, memberId, "테스트 판매자");
            Store savedStore = createStore(200L, seller, "테스트 상점");

            when(sellerJpaRepository.findByMemberId(memberId)).thenReturn(Optional.of(seller));
            when(storeJpaRepository.save(any(Store.class))).thenReturn(savedStore);

            Long storeId = storeService.createStore(memberId, request);

            assertThat(storeId).isEqualTo(200L);
            verify(storeJpaRepository).save(any(Store.class));
        }

        @Test
        @DisplayName("판매자가 아닌 회원이 상점 등록 시 UNAUTHORIZED_ACCESS 예외")
        void createStore_UNAUTHORIZED_ACCESS_exception() {

            Long memberId = 999L;
            UpsertStoreRequest request = new UpsertStoreRequest("테스트 상점");

            when(sellerJpaRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.createStore(memberId, request))
                    .isInstanceOf(StoreException.class)
                    .satisfies(ex -> assertThat(((StoreException) ex).getErrorCode())
                            .isEqualTo(StoreErrorCode.UNAUTHORIZED_ACCESS));

            verify(storeJpaRepository, never()).save(any());
        }
    }

    // 상점 조회
    @Nested
    @DisplayName("상점 조회")
    class GetStore {

        @Test
        @DisplayName("단건 조회 시 StoreResponse 반환")
        void getStore_success() {

            Long storeId = 200L;
            Seller seller = createSeller(100L, 1L, "테스트 판매자");
            Store store = createStore(storeId, seller, "테스트 상점");

            when(storeJpaRepository.findById(storeId)).thenReturn(Optional.of(store));

            StoreResponse response = storeService.getStore(storeId);

            assertThat(response.storeId()).isEqualTo(200L);
            assertThat(response.storeName()).isEqualTo("테스트 상점");
            assertThat(response.SellerName()).isEqualTo("테스트 판매자");
        }

        @Test
        @DisplayName("없는 상점 조회 시 STORE_NOT_FOUND 예외")
        void getStore_STORE_NOT_FOUND_exception() {

            Long storeId = 999L;
            when(storeJpaRepository.findById(storeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.getStore(storeId))
                    .isInstanceOf(StoreException.class)
                    .satisfies(ex -> assertThat(((StoreException) ex).getErrorCode())
                            .isEqualTo(StoreErrorCode.STORE_NOT_FOUND));
        }

        @Test
        @DisplayName("내 상점 목록 조회 성공")
        void getMyStores_success() {

            Long memberId = 1L;
            Seller seller = createSeller(100L, memberId, "테스트 판매자");
            List<Store> stores = List.of(
                    createStore(1L, seller, "상점1"),
                    createStore(2L, seller, "상점2"));

            when(sellerJpaRepository.findByMemberId(memberId)).thenReturn(Optional.of(seller));
            when(storeJpaRepository.findAllBySeller(seller)).thenReturn(stores);

            List<StoreResponse> response = storeService.getMyStores(memberId);

            assertThat(response).hasSize(2);
            assertThat(response.get(0).storeName()).isEqualTo("상점1");
            assertThat(response.get(1).storeName()).isEqualTo("상점2");
        }

        @Test
        @DisplayName("판매자 아닌 회원의 내 상점 조회 시 SELLER_NOT_FOUND 예외")
        void getMyStores_SELLER_NOT_FOUND_exception() {

            Long memberId = 999L;
            when(sellerJpaRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.getMyStores(memberId))
                    .isInstanceOf(SellerException.class)
                    .satisfies(ex -> assertThat(((SellerException) ex).getErrorCode())
                            .isEqualTo(SellerErrorCode.SELLER_NOT_FOUND));
        }

        @Test
        @DisplayName("전체 상점 목록 조회 성공")
        void getAllStores_success() {

            Seller seller = createSeller(100L, 1L, "테스트 판매자");
            List<Store> stores = List.of(
                    createStore(1L, seller, "상점1"),
                    createStore(2L, seller, "상점2"),
                    createStore(3L, seller, "상점3"));
            when(storeJpaRepository.findAll()).thenReturn(stores);

            List<StoreResponse> response = storeService.getAllStores();

            assertThat(response).hasSize(3);
        }
    }

    // 상점 수정
    @Nested
    @DisplayName("상점 수정 (updateStore)")
    class UpdateStore {

        @Test
        @DisplayName("정상 수정 시 StoreResponse 반환")
        void updateStore_success() {

            Long memberId = 1L;
            Long storeId = 200L;
            UpdateStoreRequest request = new UpdateStoreRequest("새이름");
            Seller seller = createSeller(100L, memberId, "테스트 판매자");
            Store store = createStore(storeId, seller, "기존이름");

            when(storeJpaRepository.findById(storeId)).thenReturn(Optional.of(store));

            StoreResponse response = storeService.updateStore(memberId, storeId, request);

            assertThat(response.storeName()).isEqualTo("새이름");
        }

        @Test
        @DisplayName("비소유자가 수정하면 UNAUTHORIZED_ACCESS 예외")
        void updateStore_UNAUTHORIZED_ACCESS_exception() {

            Long otherMemberId = 999L;
            Long storeId = 200L;
            UpdateStoreRequest request = new UpdateStoreRequest("새이름");
            Seller seller = createSeller(100L, 1L, "테스트 판매자"); // 소유자 memberId=1
            Store store = createStore(storeId, seller, "기존이름");

            when(storeJpaRepository.findById(storeId)).thenReturn(Optional.of(store));

            assertThatThrownBy(() -> storeService.updateStore(otherMemberId, storeId, request))
                    .isInstanceOf(StoreException.class)
                    .satisfies(ex -> assertThat(((StoreException) ex).getErrorCode())
                            .isEqualTo(StoreErrorCode.UNAUTHORIZED_ACCESS));
        }

        @Test
        @DisplayName("없는 상점 수정 시 NOT_OWNER 예외")
        void updateStore_NOT_OWNER_exception() {

            Long memberId = 1L;
            Long storeId = 999L;
            UpdateStoreRequest request = new UpdateStoreRequest("새이름");

            when(storeJpaRepository.findById(storeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.updateStore(memberId, storeId, request))
                    .isInstanceOf(StoreException.class)
                    .satisfies(ex -> assertThat(((StoreException) ex).getErrorCode())
                            .isEqualTo(StoreErrorCode.NOT_OWNER));
        }
    }

    // 상점 삭제
    @Nested
    @DisplayName("상점 삭제 (deleteStore)")
    class DeleteStore {

        @Test
        @DisplayName("정상 삭제 시 상점 삭제 호출")
        void deleteStore_success() {

            Long memberId = 1L;
            Long storeId = 200L;
            Seller seller = createSeller(100L, memberId, "테스트 판매자");
            Store store = createStore(storeId, seller, "테스트 상점");

            when(storeJpaRepository.findById(storeId)).thenReturn(Optional.of(store));

            storeService.deleteStore(memberId, storeId);

            verify(storeJpaRepository).delete(store);
        }

        @Test
        @DisplayName("비소유자가 삭제하면 UNAUTHORIZED_ACCESS 예외")
        void deleteStore_UNAUTHORIZED_ACCESS_exception() {

            Long otherMemberId = 999L;
            Long storeId = 200L;
            Seller seller = createSeller(100L, 1L, "테스트 판매자");
            Store store = createStore(storeId, seller, "테스트 상점");

            when(storeJpaRepository.findById(storeId)).thenReturn(Optional.of(store));

            assertThatThrownBy(() -> storeService.deleteStore(otherMemberId, storeId))
                    .isInstanceOf(StoreException.class)
                    .satisfies(ex -> assertThat(((StoreException) ex).getErrorCode())
                            .isEqualTo(StoreErrorCode.UNAUTHORIZED_ACCESS));

            verify(storeJpaRepository, never()).delete(any());
        }

        @Test
        @DisplayName("없는 상점 삭제 시 NOT_OWNER 예외")
        void deleteStore_NOT_OWNER_exception() {

            Long memberId = 1L;
            Long storeId = 999L;

            when(storeJpaRepository.findById(storeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.deleteStore(memberId, storeId))
                    .isInstanceOf(StoreException.class)
                    .satisfies(ex -> assertThat(((StoreException) ex).getErrorCode())
                            .isEqualTo(StoreErrorCode.NOT_OWNER));
        }
    }
}
