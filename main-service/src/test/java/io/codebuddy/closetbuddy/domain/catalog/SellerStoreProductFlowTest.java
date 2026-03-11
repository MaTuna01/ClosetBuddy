package io.codebuddy.closetbuddy.domain.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.ProductDocument;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductElasticRepository;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.sellers.repository.SellerJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.stores.repository.StoreJpaRepository;
import io.codebuddy.closetbuddy.domain.common.feign.UserServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.context.annotation.Import;
import io.codebuddy.closetbuddy.config.TestCacheConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 판매자 등록 → 상점 등록 → 상품 등록 통합 플로우 테스트
 * UserServiceClient(Feign)과 ProductElasticRepository는 @MockBean으로 처리하여
 * user-service, Elasticsearch 의존을 제거하여 테스트
 * Category는 H2에 초기화(data.sql)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestCacheConfig.class)
@DisplayName("판매자→상점→상품 등록 통합 플로우 테스트")
class SellerStoreProductFlowTest {

        private static final String USER_ROLE = "SELLER";

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private SellerJpaRepository sellerJpaRepository;

        @Autowired
        private StoreJpaRepository storeJpaRepository;

        @Autowired
        private ProductJpaRepository productJpaRepository;

        // 외부 서비스 의존 제거를 위한 MockBean
        @MockitoBean
        private UserServiceClient userServiceClient;

        @MockitoBean
        private ProductElasticRepository productElasticRepository;

        // Kafka, Redis(Redisson) 인프라 의존 제거
        @MockitoBean
        private KafkaTemplate<String, Object> kafkaTemplate;

        @MockitoBean
        private RedissonClient redissonClient;

        // 판매자 등록 테스트
        @Test
        @DisplayName("빈 이름으로 판매자 등록 시 400 Bad Request")
        void registerSeller_withInvalidName_returnsBadRequest() throws Exception {
                Map<String, Object> payload = new HashMap<>();
                payload.put("sellerName", "");

                mockMvc.perform(withUserHeaders(post("/api/v1/catalog/sellers"), 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isBadRequest());

                assertThat(sellerJpaRepository.count()).isZero();
        }

        @Test
        @DisplayName("판매자 등록 성공")
        void registerSeller_success() throws Exception {
                // Feign 호출 성공 모킹
                when(userServiceClient.grantSellerRole(anyLong()))
                                .thenReturn(ResponseEntity.ok().build());

                registerSeller(1L, "테스트 판매자");

                assertThat(sellerJpaRepository.count()).isEqualTo(1);
                assertThat(sellerJpaRepository.findByMemberId(1L)).isPresent();
        }

        // 상점 등록 테스트
        @Test
        @DisplayName("빈 이름으로 상점 등록 시 400 Bad Request")
        void createStore_withInvalidName_returnsBadRequest() throws Exception {
                when(userServiceClient.grantSellerRole(anyLong()))
                                .thenReturn(ResponseEntity.ok().build());

                registerSeller(1L, "테스트 판매자");

                Map<String, Object> payload = new HashMap<>();
                payload.put("storeName", "");

                mockMvc.perform(withUserHeaders(post("/api/v1/catalog/stores"), 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("상점 등록 성공")
        void createStore_success() throws Exception {
                when(userServiceClient.grantSellerRole(anyLong()))
                                .thenReturn(ResponseEntity.ok().build());

                registerSeller(1L, "테스트 판매자");
                Long storeId = createStore(1L, "테스트 상점");

                assertThat(storeId).isNotNull();
                assertThat(storeJpaRepository.findById(storeId)).isPresent();
        }

        // 상품 등록 테스트
        @Test
        @DisplayName("상품 등록 성공 (categoryCode 기반)")
        void createProduct_successfullyPersistsProduct() throws Exception {
                when(userServiceClient.grantSellerRole(anyLong()))
                                .thenReturn(ResponseEntity.ok().build());
                when(productElasticRepository.save(any(ProductDocument.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                registerSeller(1L, "테스트 판매자");
                Long storeId = createStore(1L, "테스트 상점");

                Map<String, Object> payload = new HashMap<>();
                payload.put("productName", "테스트 상품");
                payload.put("productPrice", 12000L);
                payload.put("productStock", 10);
                payload.put("imgUrl", "https://example.com/image.jpg");
                payload.put("categoryCode", "TSHIRT");

                mockMvc.perform(withUserHeaders(post("/api/v1/catalog/stores/" + storeId + "/products"), 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isCreated());

                assertThat(productJpaRepository.count()).isEqualTo(1);
                assertThat(productJpaRepository.findAll().get(0).getProductName()).isEqualTo("테스트 상품");
                assertThat(productJpaRepository.findAll().get(0).getCategory()).isNotNull();
                assertThat(productJpaRepository.findAll().get(0).getCategory().getCode()).isEqualTo("TSHIRT");
        }

        @Test
        @DisplayName("잘못된 상품 정보로 등록 시 400 Bad Request")
        void createProduct_withInvalidPayload_returnsBadRequest() throws Exception {
                when(userServiceClient.grantSellerRole(anyLong()))
                                .thenReturn(ResponseEntity.ok().build());

                registerSeller(1L, "테스트 판매자");
                Long storeId = createStore(1L, "테스트 상점");

                Map<String, Object> payload = new HashMap<>();
                payload.put("productName", "");
                payload.put("productPrice", null);
                payload.put("productStock", -1);
                payload.put("imgUrl", "x");
                payload.put("categoryCode", null);

                mockMvc.perform(withUserHeaders(post("/api/v1/catalog/stores/" + storeId + "/products"), 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isBadRequest());

                assertThat(productJpaRepository.count()).isZero();
        }

        // 전체 플로우 테스트
        @Test
        @DisplayName("판매자 등록 → 상점 등록 → 상품 등록 → 조회 전체 플로우")
        void fullFlow_registerAndQuery() throws Exception {
                when(userServiceClient.grantSellerRole(anyLong()))
                                .thenReturn(ResponseEntity.ok().build());
                when(productElasticRepository.save(any(ProductDocument.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // 판매자 등록
                registerSeller(1L, "플로우 판매자");

                // 상점 등록
                Long storeId = createStore(1L, "플로우 상점");

                // 상품 등록
                Map<String, Object> productPayload = new HashMap<>();
                productPayload.put("productName", "플로우 상품");
                productPayload.put("productPrice", 25000L);
                productPayload.put("productStock", 50);
                productPayload.put("imgUrl", "https://example.com/flow.jpg");
                productPayload.put("categoryCode", "JEANS");

                mockMvc.perform(withUserHeaders(post("/api/v1/catalog/stores/" + storeId + "/products"), 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(productPayload)))
                                .andExpect(status().isCreated());

                // 판매자 정보 조회
                mockMvc.perform(withUserHeaders(get("/api/v1/catalog/sellers/me"), 1L))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.sellerName").value("플로우 판매자"));

                // 상점 조회
                mockMvc.perform(get("/api/v1/catalog/stores/" + storeId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.storeName").value("플로우 상점"));

                // 상품 조회 (전체)
                mockMvc.perform(get("/api/v1/catalog/products"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].productName").value("플로우 상품"))
                                .andExpect(jsonPath("$[0].categoryCode").value("JEANS"));
        }

        // 수정/삭제 플로우
        @Test
        @DisplayName("판매자 정보 수정 성공")
        void updateSeller_success() throws Exception {
                when(userServiceClient.grantSellerRole(anyLong()))
                                .thenReturn(ResponseEntity.ok().build());

                registerSeller(1L, "원래이름");

                Map<String, Object> payload = new HashMap<>();
                payload.put("sellerName", "수정이름");

                mockMvc.perform(withUserHeaders(put("/api/v1/catalog/sellers/me"), 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isOk());

                assertThat(sellerJpaRepository.findByMemberId(1L).get().getSellerName())
                                .isEqualTo("수정이름");
        }

        @Test
        @DisplayName("상점 정보 수정 성공")
        void updateStore_success() throws Exception {
                when(userServiceClient.grantSellerRole(anyLong()))
                                .thenReturn(ResponseEntity.ok().build());

                registerSeller(1L, "테스트 판매자");
                Long storeId = createStore(1L, "기존 상점");

                Map<String, Object> payload = new HashMap<>();
                payload.put("storeName", "수정 상점");

                mockMvc.perform(withUserHeaders(put("/api/v1/catalog/stores/" + storeId), 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isOk());

                assertThat(storeJpaRepository.findById(storeId)).isPresent();
                assertThat(storeJpaRepository.findById(storeId).get().getStoreName()).isEqualTo("수정 상점");
        }

        @Test
        @DisplayName("상점 삭제 성공")
        void deleteStore_success() throws Exception {
                when(userServiceClient.grantSellerRole(anyLong()))
                                .thenReturn(ResponseEntity.ok().build());

                registerSeller(1L, "테스트 판매자");
                Long storeId = createStore(1L, "삭제 상점");

                mockMvc.perform(withUserHeaders(delete("/api/v1/catalog/stores/" + storeId), 1L))
                                .andExpect(status().isOk());

                assertThat(storeJpaRepository.findById(storeId)).isEmpty();
        }

        @Test
        @DisplayName("상품 수정 성공")
        void updateProduct_success() throws Exception {
                when(userServiceClient.grantSellerRole(anyLong()))
                                .thenReturn(ResponseEntity.ok().build());
                when(productElasticRepository.save(any(ProductDocument.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                registerSeller(1L, "테스트 판매자");
                Long storeId = createStore(1L, "테스트 상점");
                Long productId = createProduct(1L, storeId, "원본 상품", "TSHIRT");

                Map<String, Object> payload = new HashMap<>();
                payload.put("productName", "수정 상품");
                payload.put("productPrice", 19000L);
                payload.put("productStock", 4);
                payload.put("imageUrl", "https://example.com/new.jpg");
                payload.put("categoryCode", "SHIRT");

                mockMvc.perform(withUserHeaders(put("/api/v1/catalog/products/" + productId), 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isOk());

                assertThat(productJpaRepository.findById(productId)).isPresent();
                assertThat(productJpaRepository.findById(productId).get().getProductName()).isEqualTo("수정 상품");
                assertThat(productJpaRepository.findById(productId).get().getCategory().getCode()).isEqualTo("SHIRT");
        }

        @Test
        @DisplayName("상품 삭제 성공")
        void deleteProduct_success() throws Exception {
                when(userServiceClient.grantSellerRole(anyLong()))
                                .thenReturn(ResponseEntity.ok().build());
                when(productElasticRepository.save(any(ProductDocument.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                registerSeller(1L, "테스트 판매자");
                Long storeId = createStore(1L, "테스트 상점");
                Long productId = createProduct(1L, storeId, "삭제 상품", "TSHIRT");

                mockMvc.perform(withUserHeaders(delete("/api/v1/catalog/products/" + productId), 1L))
                                .andExpect(status().isOk());

                assertThat(productJpaRepository.findById(productId)).isEmpty();
        }

        @Test
        @DisplayName("내 상점 목록 및 상점별 상품 목록 조회 성공")
        void getMyStores_andProductsByStore_success() throws Exception {
                when(userServiceClient.grantSellerRole(anyLong()))
                                .thenReturn(ResponseEntity.ok().build());
                when(productElasticRepository.save(any(ProductDocument.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                registerSeller(1L, "테스트 판매자");
                Long storeId = createStore(1L, "조회 상점");
                createProduct(1L, storeId, "조회 상품", "TSHIRT");

                mockMvc.perform(withUserHeaders(get("/api/v1/catalog/stores/me"), 1L))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].storeName").value("조회 상점"));

                mockMvc.perform(get("/api/v1/catalog/products/" + storeId + "/products"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].productName").value("조회 상품"))
                                .andExpect(jsonPath("$[0].categoryCode").value("TSHIRT"));
        }

        @Test
        @DisplayName("판매자 등록 해제 성공")
        void unregisterSeller_success() throws Exception {
                when(userServiceClient.grantSellerRole(anyLong()))
                                .thenReturn(ResponseEntity.ok().build());
                when(userServiceClient.revokeSellerRole(anyLong()))
                                .thenReturn(ResponseEntity.ok().build());

                registerSeller(1L, "삭제될 판매자");

                mockMvc.perform(withUserHeaders(delete("/api/v1/catalog/sellers/me"), 1L))
                                .andExpect(status().isOk());

                assertThat(sellerJpaRepository.findByMemberId(1L)).isEmpty();
        }

        // 헬퍼 메서드
        private void registerSeller(Long memberId, String sellerName) throws Exception {
                Map<String, Object> payload = new HashMap<>();
                payload.put("sellerName", sellerName);

                mockMvc.perform(withUserHeaders(post("/api/v1/catalog/sellers"), memberId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isCreated());
        }

        private Long createStore(Long memberId, String storeName) throws Exception {
                Map<String, Object> payload = new HashMap<>();
                payload.put("storeName", storeName);

                mockMvc.perform(withUserHeaders(post("/api/v1/catalog/stores"), memberId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                // 상점 등록은 CatalogResult<Void>를 반환하므로, DB에서 직접 조회
                return storeJpaRepository.findAll().get(storeJpaRepository.findAll().size() - 1).getId();
        }

        private Long createProduct(Long memberId, Long storeId, String productName, String categoryCode) throws Exception {
                Map<String, Object> payload = new HashMap<>();
                payload.put("productName", productName);
                payload.put("productPrice", 12000L);
                payload.put("productStock", 10);
                payload.put("imgUrl", "https://example.com/product.jpg");
                payload.put("categoryCode", categoryCode);

                mockMvc.perform(withUserHeaders(post("/api/v1/catalog/stores/" + storeId + "/products"), memberId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isCreated());

                return productJpaRepository.findAll().get(productJpaRepository.findAll().size() - 1).getProductId();
        }

        private MockHttpServletRequestBuilder withUserHeaders(MockHttpServletRequestBuilder builder, Long memberId) {
                return builder
                                .header("X-User-Id", String.valueOf(memberId))
                                .header("X-User-Role", USER_ROLE);
        }
}