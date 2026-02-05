package io.codebuddy.closetbuddy.domain.catalog.product.controller;

import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class ProductControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductJpaRepository productJpaRepository;

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
