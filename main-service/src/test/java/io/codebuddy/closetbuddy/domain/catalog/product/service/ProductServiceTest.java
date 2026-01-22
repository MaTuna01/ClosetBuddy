package io.codebuddy.closetbuddy.domain.catalog.product.service;

import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.Category;
import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.ProductCreateRequest;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.products.service.ProductService;
import io.codebuddy.closetbuddy.domain.catalog.sellers.model.entity.Seller;
import io.codebuddy.closetbuddy.domain.catalog.sellers.repository.SellerJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.sellers.service.SellerService;
import io.codebuddy.closetbuddy.domain.catalog.stores.exception.StoreErrorCode;
import io.codebuddy.closetbuddy.domain.catalog.stores.exception.StoreException;
import io.codebuddy.closetbuddy.domain.catalog.stores.model.entity.Store;
import io.codebuddy.closetbuddy.domain.catalog.stores.repository.StoreJpaRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @Mock
    private ProductJpaRepository productJpaRepository;

    @Mock
    private StoreJpaRepository storeJpaRepository;

    @Mock
    private SellerJpaRepository sellerJpaRepository;

    @Mock
    private SellerService sellerService;

    @InjectMocks
    private ProductService productService;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void createProduct_returnsProductId_whenRequestValidAndOwnerMatches() {
        Long memberId = 1L;
        Seller seller = Seller.builder()
                .sellerId(10L)
                .memberId(memberId)
                .sellerName("판매자")
                .build();
        Store store = Store.builder()
                .id(20L)
                .seller(seller)
                .storeName("상점")
                .build();
        ProductCreateRequest request = new ProductCreateRequest(
                "셔츠",
                15000L,
                5,
                "https://example.com/image.png",
                Category.TOP
        );

        when(storeJpaRepository.findById(store.getId())).thenReturn(Optional.of(store));
        when(productJpaRepository.save(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product saved = invocation.getArgument(0);
                    saved.setProductId(777L);
                    return saved;
                });

        Long productId = productService.createProduct(memberId, store.getId(), request);

        assertThat(productId).isEqualTo(777L);
    }

    @Test
    void createProduct_throwsException_whenNotStoreOwner() {
        Long memberId = 999L;
        Seller seller = Seller.builder()
                .sellerId(10L)
                .memberId(1L)
                .sellerName("판매자")
                .build();
        Store store = Store.builder()
                .id(20L)
                .seller(seller)
                .storeName("상점")
                .build();
        ProductCreateRequest request = new ProductCreateRequest(
                "셔츠",
                15000L,
                5,
                "https://example.com/image.png",
                Category.TOP
        );

        when(storeJpaRepository.findById(store.getId())).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> productService.createProduct(memberId, store.getId(), request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("본인의 상점에만 상품을 등록할 수 있습니다");
    }

    @Test
    void createProduct_throwsException_whenStoreMissing() {
        Long memberId = 1L;
        ProductCreateRequest request = new ProductCreateRequest(
                "셔츠",
                15000L,
                5,
                "https://example.com/image.png",
                Category.TOP
        );

        when(storeJpaRepository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(memberId, 55L, request))
                .isInstanceOf(StoreException.class)
                .satisfies(ex -> assertThat(((StoreException) ex).getErrorCode())
                        .isEqualTo(StoreErrorCode.STORE_NOT_FOUND));
    }

    @Test
    void createProductRequest_hasViolations_forInvalidValues() {
        ProductCreateRequest request = new ProductCreateRequest(
                " ",
                -1L,
                -3,
                "aa",
                null
        );

        Set<ConstraintViolation<ProductCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void createProductRequest_hasNoViolations_forValidValues() {
        ProductCreateRequest request = new ProductCreateRequest(
                "맨투맨",
                30000L,
                10,
                "https://example.com/valid.png",
                Category.TOP
        );

        Set<ConstraintViolation<ProductCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
