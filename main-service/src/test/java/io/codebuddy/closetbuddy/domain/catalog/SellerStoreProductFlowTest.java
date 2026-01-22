package io.codebuddy.closetbuddy.domain.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.sellers.repository.SellerJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.stores.repository.StoreJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
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

    @Test
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
    void createStore_withInvalidName_returnsBadRequest() throws Exception {
        registerSeller(1L, "테스트 판매자");

        Map<String, Object> payload = new HashMap<>();
        payload.put("storeName", "");

        mockMvc.perform(withUserHeaders(post("/api/v1/catalog/stores"), 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        assertThat(storeJpaRepository.count()).isZero();
    }

    @Test
    void createProduct_successfullyPersistsProduct() throws Exception {
        registerSeller(1L, "테스트 판매자");
        Long storeId = createStore(1L, "테스트 상점");

        Map<String, Object> payload = new HashMap<>();
        payload.put("productName", "테스트 상품");
        payload.put("productPrice", 12000L);
        payload.put("productStock", 10);
        payload.put("imgUrl", "https://example.com/image.jpg");
        payload.put("category", "TOP");

        mockMvc.perform(withUserHeaders(post("/api/v1/catalog/stores/" + storeId + "/products"), 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        assertThat(productJpaRepository.count()).isEqualTo(1);
        assertThat(productJpaRepository.findAll().get(0).getProductName()).isEqualTo("테스트 상품");
    }

    @Test
    void createProduct_withInvalidPayload_returnsBadRequest() throws Exception {
        registerSeller(1L, "테스트 판매자");
        Long storeId = createStore(1L, "테스트 상점");

        Map<String, Object> payload = new HashMap<>();
        payload.put("productName", "");
        payload.put("productPrice", null);
        payload.put("productStock", -1);
        payload.put("imgUrl", "x");
        payload.put("category", null);

        mockMvc.perform(withUserHeaders(post("/api/v1/catalog/stores/" + storeId + "/products"), 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        assertThat(productJpaRepository.count()).isZero();
    }

    private void registerSeller(Long memberId, String sellerName) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sellerName", sellerName);

        mockMvc.perform(withUserHeaders(post("/api/v1/catalog/sellers"), memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        assertThat(sellerJpaRepository.count()).isEqualTo(1);
    }

    private Long createStore(Long memberId, String storeName) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("storeName", storeName);

        String response = mockMvc.perform(withUserHeaders(post("/api/v1/catalog/stores"), memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return Long.valueOf(response);
    }

    private MockHttpServletRequestBuilder withUserHeaders(MockHttpServletRequestBuilder builder, Long memberId) {
        return builder
                .header("X-User-Id", String.valueOf(memberId))
                .header("X-User-Role", USER_ROLE);
    }
}
