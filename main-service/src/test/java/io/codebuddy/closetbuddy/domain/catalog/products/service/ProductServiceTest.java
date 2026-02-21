package io.codebuddy.closetbuddy.domain.catalog.products.service;

import io.codebuddy.closetbuddy.domain.catalog.category.exception.CategoryException;
import io.codebuddy.closetbuddy.domain.catalog.category.model.entity.Category;
import io.codebuddy.closetbuddy.domain.catalog.products.exception.ProductErrorCode;
import io.codebuddy.closetbuddy.domain.catalog.products.exception.ProductException;
import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.InternalProductResponse;
import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.ProductCreateRequest;
import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.ProductSearchResponse;
import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.ProductResponse;
import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.UpdateProductRequest;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.ProductDocument;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.CategoryJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductElasticRepository;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.sellers.model.entity.Seller;
import io.codebuddy.closetbuddy.domain.catalog.stores.exception.StoreErrorCode;
import io.codebuddy.closetbuddy.domain.catalog.stores.exception.StoreException;
import io.codebuddy.closetbuddy.domain.catalog.stores.model.entity.Store;
import io.codebuddy.closetbuddy.domain.catalog.stores.repository.StoreJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static io.codebuddy.closetbuddy.domain.catalog.category.exception.CategoryErrorCode.CATEGORY_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 단위 테스트")
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductJpaRepository productJpaRepository;

    @Mock
    private StoreJpaRepository storeJpaRepository;

    @Mock
    private CategoryJpaRepository categoryJpaRepository;

    @Mock
    private ProductElasticRepository productElasticRepository;

    // 헬퍼 메서드
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

    private Category createCategory(Long categoryId, String name, String code, Category parent) {
        try {
            java.lang.reflect.Constructor<Category> constructor = Category.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Category category = constructor.newInstance();
            setField(category, "categoryId", categoryId);
            setField(category, "name", name);
            setField(category, "code", code);
            setField(category, "parent", parent);
            return category;
        } catch (Exception e) {
            throw new CategoryException(CATEGORY_NOT_FOUND);
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Product createProduct(Long productId, String name, Long price, int stock,
            Store store, String imageUrl, Category category) {
        return Product.builder()
                .productId(productId)
                .productName(name)
                .productPrice(price)
                .productStock(stock)
                .store(store)
                .imageUrl(imageUrl)
                .category(category)
                .build();
    }

    private ProductDocument createProductDocument(String id, String name, Long price, int stock, String storeName, String categoryName) {
        return ProductDocument.builder()
                .id(id)
                .productName(name)
                .productPrice(price)
                .productStock(stock)
                .storeName(storeName)
                .categoryName(categoryName)
                .imageUrl("https://img.jpg")
                .build();
    }

    // 상품 등록
    @Nested
    @DisplayName("상품 등록 (createProduct)")
    class CreateProduct {

        @Test
        @DisplayName("정상 등록 시 상품 및 ELS 저장")
        void createProduct_success() {

            Long memberId = 1L;
            Long storeId = 200L;
            Seller seller = createSeller(100L, memberId, "테스트 판매자");
            Store store = createStore(storeId, seller, "테스트 상점");
            Category parentCategory = createCategory(1L, "상의", "TOP", null);
            Category category = createCategory(3L, "티셔츠", "TSHIRT", parentCategory);

            ProductCreateRequest request = new ProductCreateRequest(
                    "테스트 상품", 12000L, 10, "https://example.com/img.jpg", "TSHIRT");

            when(storeJpaRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(categoryJpaRepository.findByCode("TSHIRT")).thenReturn(Optional.of(category));
            when(productJpaRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product p = invocation.getArgument(0);
                setField(p, "productId", 300L);
                return p;
            });

            productService.createProduct(memberId, storeId, request);

            verify(productJpaRepository).save(any(Product.class));
            verify(productElasticRepository).save(any(ProductDocument.class));
        }

        @Test
        @DisplayName("없는 상점에 등록 시 STORE_NOT_FOUND 예외")
        void createProduct_STORE_NOT_FOUND_exception() {

            Long memberId = 1L;
            Long storeId = 999L;
            ProductCreateRequest request = new ProductCreateRequest(
                    "테스트 상품", 12000L, 10, "https://example.com/img.jpg", "TSHIRT");

            when(storeJpaRepository.findById(storeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.createProduct(memberId, storeId, request))
                    .isInstanceOf(StoreException.class)
                    .satisfies(ex -> assertThat(((StoreException) ex).getErrorCode())
                            .isEqualTo(StoreErrorCode.STORE_NOT_FOUND));
        }

        @Test
        @DisplayName("없는 카테고리 코드 시 CATEGORY_NOT_FOUND 예외")
        void createProduct_CATEGORY_NOT_FOUND_exception() {

            Long memberId = 1L;
            Long storeId = 200L;
            Seller seller = createSeller(100L, memberId, "테스트 판매자");
            Store store = createStore(storeId, seller, "테스트 상점");

            ProductCreateRequest request = new ProductCreateRequest(
                    "테스트 상품", 12000L, 10, "https://example.com/img.jpg", "INVALID_CODE");

            when(storeJpaRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(categoryJpaRepository.findByCode("INVALID_CODE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.createProduct(memberId, storeId, request))
                    .isInstanceOf(CategoryException.class)
                    .satisfies(ex -> assertThat(((CategoryException) ex).getErrorCode())
                            .isEqualTo(CATEGORY_NOT_FOUND));
        }

        @Test
        @DisplayName("비소유자가 등록하면 IllegalStateException 예외")
        void createProduct_ILLEGAL_STATE_exception() {

            Long otherMemberId = 999L;
            Long storeId = 200L;
            Seller seller = createSeller(100L, 1L, "테스트 판매자"); // 소유자 memberId=1
            Store store = createStore(storeId, seller, "테스트 상점");

            ProductCreateRequest request = new ProductCreateRequest(
                    "테스트 상품", 12000L, 10, "https://example.com/img.jpg", "TSHIRT");

            when(storeJpaRepository.findById(storeId)).thenReturn(Optional.of(store));
            // validateStoreOwner가 categoryJpaRepository.findByCode 보다 먼저 호출되므로
            // category 스텁은 필요 X

            assertThatThrownBy(() -> productService.createProduct(otherMemberId, storeId, request))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // 상품 수정
    @Nested
    @DisplayName("상품 수정 (updateProduct)")
    class UpdateProduct {

        @Test
        @DisplayName("정상 수정 시 상품 및 ELS 업데이트")
        void updateProduct_성공() {

            Long memberId = 1L;
            Long productId = 300L;
            Seller seller = createSeller(100L, memberId, "테스트 판매자");
            Store store = createStore(200L, seller, "테스트 상점");
            Category oldCategory = createCategory(3L, "티셔츠", "TSHIRT", null);
            Category newCategory = createCategory(4L, "셔츠", "SHIRT", null);

            Product product = createProduct(productId, "기존상품", 10000L, 5, store, "https://old.jpg", oldCategory);

            UpdateProductRequest request = new UpdateProductRequest(
                    "수정상품", 15000L, 20, "https://new.jpg", "SHIRT");

            when(productJpaRepository.findById(productId)).thenReturn(Optional.of(product));
            when(categoryJpaRepository.findByCode("SHIRT")).thenReturn(Optional.of(newCategory));

            productService.updateProduct(memberId, productId, request);

            assertThat(product.getProductName()).isEqualTo("수정상품");
            assertThat(product.getProductPrice()).isEqualTo(15000L);
            assertThat(product.getProductStock()).isEqualTo(20);
            assertThat(product.getImageUrl()).isEqualTo("https://new.jpg");
            assertThat(product.getCategory()).isEqualTo(newCategory);
            verify(productElasticRepository).save(any(ProductDocument.class));
        }

        @Test
        @DisplayName("없는 상품 수정 시 PRODUCT_NOT_FOUND 예외")
        void updateProduct_PRODUCT_NOT_FOUND_exception() {

            Long memberId = 1L;
            Long productId = 999L;
            UpdateProductRequest request = new UpdateProductRequest(
                    "수정상품", 15000L, 20, "https://new.jpg", "SHIRT");

            when(productJpaRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateProduct(memberId, productId, request))
                    .isInstanceOf(ProductException.class)
                    .satisfies(ex -> assertThat(((ProductException) ex).getErrorCode())
                            .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND));
        }

        @Test
        @DisplayName("비소유자가 수정하면 NOT_OWNER 예외")
        void updateProduct_비소유자_예외() {

            Long otherMemberId = 999L;
            Long productId = 300L;
            Seller seller = createSeller(100L, 1L, "테스트 판매자");
            Store store = createStore(200L, seller, "테스트 상점");
            Category category = createCategory(3L, "티셔츠", "TSHIRT", null);
            Category newCategory = createCategory(4L, "셔츠", "SHIRT", null);

            Product product = createProduct(productId, "기존상품", 10000L, 5, store, "https://old.jpg", category);

            UpdateProductRequest request = new UpdateProductRequest(
                    "수정상품", 15000L, 20, "https://new.jpg", "SHIRT");

            when(productJpaRepository.findById(productId)).thenReturn(Optional.of(product));
            when(categoryJpaRepository.findByCode("SHIRT")).thenReturn(Optional.of(newCategory));

            assertThatThrownBy(() -> productService.updateProduct(otherMemberId, productId, request))
                    .isInstanceOf(ProductException.class)
                    .satisfies(ex -> assertThat(((ProductException) ex).getErrorCode())
                            .isEqualTo(ProductErrorCode.NOT_OWNER));
        }
    }

    // 상품 조회
    @Nested
    @DisplayName("상품 조회")
    class GetProduct {

        @Test
        @DisplayName("단건 조회 시 ProductResponse 반환 (카테고리 정보 포함)")
        void getProduct_success() {

            Long productId = 300L;
            Seller seller = createSeller(100L, 1L, "테스트 판매자");
            Store store = createStore(200L, seller, "테스트 상점");
            Category parentCategory = createCategory(1L, "상의", "TOP", null);
            Category category = createCategory(3L, "티셔츠", "TSHIRT", parentCategory);
            Product product = createProduct(productId, "테스트 상품", 12000L, 10, store, "https://img.jpg", category);

            when(productJpaRepository.findById(productId)).thenReturn(Optional.of(product));

            ProductResponse response = productService.getProduct(productId);

            assertThat(response.productId()).isEqualTo(300L);
            assertThat(response.productName()).isEqualTo("테스트 상품");
            assertThat(response.productPrice()).isEqualTo(12000L);
            assertThat(response.productStock()).isEqualTo(10);
            assertThat(response.categoryCode()).isEqualTo("TSHIRT");
            assertThat(response.categoryName()).isEqualTo("티셔츠");
            assertThat(response.parentCategoryCode()).isEqualTo("TOP");
            assertThat(response.storeName()).isEqualTo("테스트 상점");
        }

        @Test
        @DisplayName("없는 상품 조회 시 PRODUCT_NOT_FOUND 예외")
        void getProduct_PRODUCT_NOT_FOUND_exception() {

            Long productId = 999L;
            when(productJpaRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProduct(productId))
                    .isInstanceOf(ProductException.class)
                    .satisfies(ex -> assertThat(((ProductException) ex).getErrorCode())
                            .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND));
        }

        @Test
        @DisplayName("특정 상점의 상품 리스트 조회 성공")
        void getProductByStoreId_success() {
            // given
            Long storeId = 200L;
            Seller seller = createSeller(100L, 1L, "테스트 판매자");
            Store store = createStore(storeId, seller, "테스트 상점");
            Category category = createCategory(3L, "티셔츠", "TSHIRT", null);

            List<Product> products = List.of(
                    createProduct(1L, "상품1", 10000L, 5, store, "https://img1.jpg", category),
                    createProduct(2L, "상품2", 20000L, 3, store, "https://img2.jpg", category));

            when(storeJpaRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(productJpaRepository.findByStoreId(storeId)).thenReturn(products);

            List<ProductResponse> response = productService.getProductByStoreId(storeId);

            assertThat(response).hasSize(2);
            assertThat(response.get(0).productName()).isEqualTo("상품1");
            assertThat(response.get(1).productName()).isEqualTo("상품2");
        }

        @Test
        @DisplayName("없는 상점의 상품 조회 시 STORE_NOT_FOUND 예외")
        void getProductByStoreId_STORE_NOT_FOUND_exception() {

            Long storeId = 999L;
            when(storeJpaRepository.findById(storeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductByStoreId(storeId))
                    .isInstanceOf(StoreException.class)
                    .satisfies(ex -> assertThat(((StoreException) ex).getErrorCode())
                            .isEqualTo(StoreErrorCode.STORE_NOT_FOUND));
        }

        @Test
        @DisplayName("전체 상품 리스트 조회 성공")
        void getAllProducts_success() {

            Seller seller = createSeller(100L, 1L, "테스트 판매자");
            Store store = createStore(200L, seller, "테스트 상점");
            Category category = createCategory(3L, "티셔츠", "TSHIRT", null);

            List<Product> products = List.of(
                    createProduct(1L, "상품1", 10000L, 5, store, "https://img1.jpg", category),
                    createProduct(2L, "상품2", 20000L, 3, store, "https://img2.jpg", category),
                    createProduct(3L, "상품3", 30000L, 1, store, "https://img3.jpg", category));
            when(productJpaRepository.findAll()).thenReturn(products);

            List<ProductResponse> response = productService.getAllProducts();

            assertThat(response).hasSize(3);
        }
    }

    @Nested
    @DisplayName("내부 조회/검색")
    class InternalAndSearch {

        @Test
        @DisplayName("내부 상품 조회 성공")
        void getInternalProduct_success() {
            Long productId = 300L;
            Seller seller = createSeller(100L, 1L, "테스트 판매자");
            Store store = createStore(200L, seller, "테스트 상점");
            Category category = createCategory(3L, "티셔츠", "TSHIRT", null);
            Product product = createProduct(productId, "테스트 상품", 12000L, 10, store, "https://img.jpg", category);

            when(productJpaRepository.findById(productId)).thenReturn(Optional.of(product));

            InternalProductResponse response = productService.getInternalProduct(productId);

            assertThat(response.productId()).isEqualTo(productId);
            assertThat(response.productName()).isEqualTo("테스트 상품");
            assertThat(response.storeId()).isEqualTo(200L);
            assertThat(response.sellerId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("없는 내부 상품 조회 시 PRODUCT_NOT_FOUND 예외")
        void getInternalProduct_PRODUCT_NOT_FOUND_exception() {
            when(productJpaRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getInternalProduct(999L))
                    .isInstanceOf(ProductException.class)
                    .satisfies(ex -> assertThat(((ProductException) ex).getErrorCode())
                            .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND));
        }

        @Test
        @DisplayName("자동완성 조회 시 중복 상품명 제거")
        void getSuggestions_DUPLICATION() {
            SearchPage<ProductDocument> searchPage = mock(SearchPage.class);
            SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
            SearchHit<ProductDocument> hit1 = mock(SearchHit.class);
            SearchHit<ProductDocument> hit2 = mock(SearchHit.class);
            SearchHit<ProductDocument> hit3 = mock(SearchHit.class);

            ProductDocument nike1 = createProductDocument("1", "나이키 반팔", 10000L, 3, "A상점", "티셔츠");
            ProductDocument nike2 = createProductDocument("2", "나이키 반팔", 11000L, 4, "B상점", "티셔츠");
            ProductDocument adidas = createProductDocument("3", "아디다스 반팔", 9000L, 2, "C상점", "티셔츠");

            when(hit1.getContent()).thenReturn(nike1);
            when(hit2.getContent()).thenReturn(nike2);
            when(hit3.getContent()).thenReturn(adidas);
            when(searchHits.iterator()).thenReturn(List.of(hit1, hit2, hit3).iterator());
            when(searchPage.getSearchHits()).thenReturn(searchHits);
            when(productElasticRepository.autoComplete(eq("나"), any(Pageable.class))).thenReturn(searchPage);

            List<String> suggestions = productService.getSuggestions("나", 5);

            assertThat(suggestions).containsExactly("나이키 반팔", "아디다스 반팔");
            verify(productElasticRepository).autoComplete(eq("나"), eq(PageRequest.of(0, 5)));
        }

        @Test
        @DisplayName("상품 검색 결과를 ProductSearchResponse로 매핑")
        void searchProducts_success() {
            Pageable pageable = PageRequest.of(0, 10);
            SearchPage<ProductDocument> searchPage = mock(SearchPage.class);
            SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
            SearchHit<ProductDocument> hit1 = mock(SearchHit.class);
            SearchHit<ProductDocument> hit2 = mock(SearchHit.class);

            ProductDocument doc1 = createProductDocument("1", "티셔츠1", 10000L, 5, "상점1", "티셔츠");
            ProductDocument doc2 = createProductDocument("2", "티셔츠2", 20000L, 3, "상점2", "티셔츠");

            when(hit1.getContent()).thenReturn(doc1);
            when(hit2.getContent()).thenReturn(doc2);
            when(searchHits.iterator()).thenReturn(List.of(hit1, hit2).iterator());
            when(searchPage.getSearchHits()).thenReturn(searchHits);
            when(searchPage.getTotalElements()).thenReturn(2L);
            when(productElasticRepository.searchByKeyword("티셔츠", pageable)).thenReturn(searchPage);

            Page<ProductSearchResponse> result = productService.searchProducts("티셔츠", pageable);

            assertThat(result.getTotalElements()).isEqualTo(2L);
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).productName()).isEqualTo("티셔츠1");
            assertThat(result.getContent().get(1).storeName()).isEqualTo("상점2");
        }
    }

    // 상품 삭제
    @Nested
    @DisplayName("상품 삭제 (deleteProduct)")
    class DeleteProduct {

        @Test
        @DisplayName("정상 삭제 시 상품 및 ELS 삭제")
        void deleteProduct_success() {

            Long memberId = 1L;
            Long productId = 300L;
            Seller seller = createSeller(100L, memberId, "테스트 판매자");
            Store store = createStore(200L, seller, "테스트 상점");
            Category category = createCategory(3L, "티셔츠", "TSHIRT", null);
            Product product = createProduct(productId, "테스트 상품", 12000L, 10, store, "https://img.jpg", category);

            when(productJpaRepository.findById(productId)).thenReturn(Optional.of(product));

            productService.deleteProduct(memberId, productId);

            verify(productJpaRepository).delete(product);
            verify(productElasticRepository).delete(any(ProductDocument.class));
        }

        @Test
        @DisplayName("비소유자가 삭제하면 NOT_OWNER 예외")
        void deleteProduct_NOT_OWNER_exception() {

            Long otherMemberId = 999L;
            Long productId = 300L;
            Seller seller = createSeller(100L, 1L, "테스트 판매자");
            Store store = createStore(200L, seller, "테스트 상점");
            Category category = createCategory(3L, "티셔츠", "TSHIRT", null);
            Product product = createProduct(productId, "테스트 상품", 12000L, 10, store, "https://img.jpg", category);

            when(productJpaRepository.findById(productId)).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> productService.deleteProduct(otherMemberId, productId))
                    .isInstanceOf(ProductException.class)
                    .satisfies(ex -> assertThat(((ProductException) ex).getErrorCode())
                            .isEqualTo(ProductErrorCode.NOT_OWNER));

            verify(productJpaRepository, never()).delete(any());
        }

        @Test
        @DisplayName("없는 상품 삭제 시 PRODUCT_NOT_FOUND 예외")
        void deleteProduct_PRODUCT_NOT_FOUND_exception() {

            Long memberId = 1L;
            Long productId = 999L;

            when(productJpaRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(memberId, productId))
                    .isInstanceOf(ProductException.class)
                    .satisfies(ex -> assertThat(((ProductException) ex).getErrorCode())
                            .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND));
        }
    }
}
