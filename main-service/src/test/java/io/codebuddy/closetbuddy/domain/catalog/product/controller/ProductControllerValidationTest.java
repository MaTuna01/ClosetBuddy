package io.codebuddy.closetbuddy.domain.catalog.product.controller;

import io.codebuddy.closetbuddy.config.TestCacheConfig;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductElasticRepository;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductJpaRepository;
import io.codebuddy.closetbuddy.domain.common.feign.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestCacheConfig.class)
// 유효값 검증 테스트 코드
class ProductControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @MockitoBean
    private ProductElasticRepository productElasticRepository;

    @MockitoBean
    private UserServiceClient userServiceClient;

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private RedissonClient redissonClient;

    @Test
    void createProductRejectsInvalidRequest() throws Exception {
        String invalidPayload = """
                {
                  \"productName\": \"\",
                  \"productPrice\": -1000,
                  \"productStock\": -3,
                  \"imgUrl\": \"a\",
                  \"category\": null
                }
                """;

        mockMvc.perform(post("/api/v1/catalog/stores/1/products")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "SELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());

        assertThat(productJpaRepository.count()).isZero();
    }
}
