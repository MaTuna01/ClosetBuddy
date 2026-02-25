package io.codebuddy.closetbuddy.domain.catalog.products.service;

import io.codebuddy.closetbuddy.domain.catalog.category.exception.CategoryErrorCode;
import io.codebuddy.closetbuddy.domain.catalog.category.exception.CategoryException;
import io.codebuddy.closetbuddy.domain.catalog.products.exception.ProductErrorCode;
import io.codebuddy.closetbuddy.domain.catalog.products.exception.ProductException;
import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.InternalProductResponse;
import io.codebuddy.closetbuddy.domain.catalog.category.model.entity.Category;
import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.ProductResponse;
import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.UpdateProductRequest;
import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.ProductCreateRequest;
import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.*;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.CategoryJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.ProductDocument;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductElasticRepository;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductJpaRepository;

import io.codebuddy.closetbuddy.domain.catalog.stores.exception.StoreErrorCode;
import io.codebuddy.closetbuddy.domain.catalog.stores.exception.StoreException;
import io.codebuddy.closetbuddy.domain.catalog.stores.model.entity.Store;
import io.codebuddy.closetbuddy.domain.catalog.stores.repository.StoreJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductJpaRepository productJpaRepository;
    private final StoreJpaRepository storeJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;
    private final ProductElasticRepository productElasticRepository;

    // 상품 등록
    @CacheEvict(value = { "product:all, product:store" }, allEntries = true)
    @Transactional
    public void createProduct(Long memberId, Long storeId, ProductCreateRequest request) {
        Store store = storeJpaRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        // 검증 로직(로그인 한 사람이 이 상품을 올릴 상점 주인이 맞는지?
        validateStoreOwner(memberId, store);

        // 서비스에서 Category 조회
        Category category = categoryJpaRepository.findByCode(request.categoryCode())
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        // 조회한 카테고리와 입력받은 request DTO를 통해 product 생성
        // toEntity에 Category 전달
        Product product = request.toEntity(store, category);
        productJpaRepository.save(product);

        // ELS 상품 등록
        ProductDocument productDocument = ProductMapper.toProductDocument(product);
        productElasticRepository.save(productDocument);
    }

    // 상품 수정
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#productId"),
            @CacheEvict(value = { "products:all", "products:store" }, allEntries = true)
    })
    @Transactional
    public void updateProduct(Long memberId, Long productId, UpdateProductRequest request) {
        Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // 서비스에서 Category 조회
        Category category = categoryJpaRepository.findByCode(request.categoryCode())
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        validateProductOwner(memberId, product);

        product.update(
                request.productName(),
                request.productPrice(),
                request.productStock(),
                product.getStore(),
                request.imageUrl(),
                category);

        // ELS 수정
        ProductDocument productDocument = ProductMapper.toProductDocument(product);

        // ELS의 save : 없으면 create, 있으면 update
        productElasticRepository.save(productDocument);
    }

    // 상품 상세조회(단건)
    @Cacheable(value = "product", key = "#productId")
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        return ProductResponse.from(product);
    }

    // 특정 가게의 상품 목록 조회 (상점 페이지)
    @Cacheable(value = "products:store", key = "#storeId")
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductByStoreId(Long storeId) {
        if (storeJpaRepository.findById(storeId).isEmpty()) {
            throw new StoreException(StoreErrorCode.STORE_NOT_FOUND);
        }
        return productJpaRepository.findByStoreId(storeId).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    // 전체 상품목록 조회
    @Cacheable(value = "products:all")
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productJpaRepository.findAll().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    // 상품 삭제
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#productId"),
            @CacheEvict(value = { "products:all", "products:store" }, allEntries = true)
    })
    @Transactional
    public void deleteProduct(Long memberId, Long productId) {
        Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // 상품을 삭제할 권한이 있는 회원인지 검증
        validateProductOwner(memberId, product);
        productJpaRepository.delete(product);

        // ELS 데이터 삭제
        ProductDocument productDocument = ProductMapper.toProductDocument(product);
        productElasticRepository.delete(productDocument);
    }

    // 검색 자동 완성
    public List<String> getSuggestions(String prefix, Integer limit) {

        // Pageable로 개수 제한
        Pageable pageable = PageRequest.of(0, limit);
        // SearchPage : SearchHits + Page
        SearchPage<ProductDocument> searchPage = productElasticRepository.autoComplete(prefix, pageable);
        // 검색된 결과에서 searchHits만 분리
        SearchHits<ProductDocument> searchHits = searchPage.getSearchHits();

        // 결과 담을 리스트와 중복 확인용 Set 생성
        // '나이키' 검색 시 '나이키 신발'이 100개 출력되지 않도록 중복 제거
        List<String> resultList = new ArrayList<>();
        Set<String> uniqueChecker = new HashSet<>();

        for (SearchHit<ProductDocument> hit : searchHits) {
            ProductDocument product = hit.getContent();
            String name = product.getProductName();

            // 중복 제거 로직 (Set에 없으면 추가)
            if (!uniqueChecker.contains(name)) {
                uniqueChecker.add(name); // Set에 등록
                resultList.add(name); // 결과 리스트에 추가
            }
        }

        return resultList;
    }

    // 상품 검색
    public Page<ProductSearchResponse> searchProducts(String keyword, Pageable pageable) {

        // ELS에서 검색 결과 가져오기
        SearchPage<ProductDocument> searchPage = productElasticRepository.searchByKeyword(keyword, pageable);

        return searchToPage(searchPage, pageable);
    }

    // 하위 카테고리 내에서 검색
    public Page<ProductSearchResponse> searchProductsByCategoryAndKeyword(String category, String keyword,
            Pageable pageable) {

        SearchPage<ProductDocument> searchPage;

        // 키워드가 존재하는지 확인
        if (StringUtils.hasText(keyword)) { // null,공백 확인
            // 카테고리 선택 후 키워드 검색
            searchPage = productElasticRepository.searchByCategoryAndKeyword(category, keyword, pageable);
        } else {
            // 카테고리만 선택한 경우 (해당 카테고리의 전체 상품 조회)
            searchPage = productElasticRepository.searchByCategory(category, pageable);
        }
        return searchToPage(searchPage, pageable);
    }

    // searchPage -> Response 변환
    private Page<ProductSearchResponse> searchToPage(SearchPage<ProductDocument> searchPage, Pageable pageable) {

        // 결과를 담을 리스트 생성
        List<ProductSearchResponse> responseList = new ArrayList<>();

        for (SearchHit<ProductDocument> hit : searchPage.getSearchHits()) {
            ProductDocument document = hit.getContent();

            ProductSearchResponse dto = ProductSearchResponse.fromDocument(document);

            responseList.add(dto);
        }

        return new PageImpl<>(responseList, pageable, searchPage.getTotalElements());
    }

    // (상점 주인 확인용) 검증 로직
    private void validateStoreOwner(Long memberId, Store store) {
        if (!store.getSeller().getMemberId().equals(memberId)) {
            throw new IllegalStateException("본인의 상점에만 상품을 등록할 수 있습니다.");
        }
    }

    // (상품 주인 확인용) 검증로직
    private void validateProductOwner(Long memberId, Product product) {
        Long ownerId = product.getStore().getSeller().getMemberId();
        if (!ownerId.equals(memberId)) {
            throw new ProductException(ProductErrorCode.NOT_OWNER);
        }
    }

    // Order-Service 에서 사용하는 조회 메서드 추가
    @Transactional(readOnly = true)
    public InternalProductResponse getInternalProduct(Long productId) {
        Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        return InternalProductResponse.from(product);
    }

    // Order-Service 의 추천 시스템에서 사용하는 조회 메서드 추가
    @Transactional
    public List<InternalRecommendProductResponse> getInternalProducts(List<Long> productIds) {
        List<Product> products = productJpaRepository.findRecommendProductWithStore(productIds);

        return products.stream()
                .map(InternalRecommendProductResponse::from)
                .collect(Collectors.toList());
    }
}
