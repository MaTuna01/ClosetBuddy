package io.codebuddy.closetbuddy.domain.catalog;

import io.codebuddy.closetbuddy.domain.catalog.category.model.entity.Category;
import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.ProductCreateRequest;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.products.service.ProductService;
import io.codebuddy.closetbuddy.domain.catalog.sellers.model.dto.SellerUpsertRequest;
import io.codebuddy.closetbuddy.domain.catalog.sellers.service.SellerService;
import io.codebuddy.closetbuddy.domain.catalog.stores.model.dto.UpsertStoreRequest;
import io.codebuddy.closetbuddy.domain.catalog.stores.model.entity.Store;
import io.codebuddy.closetbuddy.domain.catalog.stores.repository.StoreJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.stores.service.StoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CatalogProductFlowTest {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Test
    void productIsCreatedWithSellerAndStore() {
        Long memberId = 1L;
        SellerUpsertRequest sellerRequest = new SellerUpsertRequest("판매자김");
        sellerService.registerSeller(memberId, sellerRequest);

        UpsertStoreRequest storeRequest = new UpsertStoreRequest("테스트 상점");
        Long storeId = storeService.createStore(memberId, storeRequest);

        ProductCreateRequest productRequest = new ProductCreateRequest(
                "데님 셔츠",
                32000L,
                10,
                "https://example.com/images/item.png",
                Category.TOP
        );

        Long productId = productService.createProduct(memberId, storeId, productRequest);

        Product product = productJpaRepository.findById(productId).orElseThrow();
        Store store = storeJpaRepository.findById(storeId).orElseThrow();

        assertThat(product.getProductName()).isEqualTo("데님 셔츠");
        assertThat(product.getProductPrice()).isEqualTo(32000L);
        assertThat(product.getProductStock()).isEqualTo(10);
        assertThat(product.getImageUrl()).isEqualTo("https://example.com/images/item.png");
        assertThat(product.getCategory()).isEqualTo(Category.TOP);
        assertThat(product.getStore().getId()).isEqualTo(store.getId());
        assertThat(store.getSeller().getMemberId()).isEqualTo(memberId);
    }
}
