package io.codebuddy.closetbuddy.domain.products.service;

import io.codebuddy.closetbuddy.domain.products.model.dto.ProductResponse;
import io.codebuddy.closetbuddy.domain.products.model.dto.UpdateProductRequest;
import io.codebuddy.closetbuddy.domain.products.model.dto.ProductCreateRequest;
import io.codebuddy.closetbuddy.domain.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.products.repository.ProductJpaRepository;

import io.codebuddy.closetbuddy.domain.sellers.repository.SellerJpaRepository;
import io.codebuddy.closetbuddy.domain.sellers.service.SellerService;
import io.codebuddy.closetbuddy.domain.stores.model.entity.Store;
import io.codebuddy.closetbuddy.domain.stores.repository.StoreJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductJpaRepository productJpaRepository;
    private final StoreJpaRepository storeJpaRepository;
    private final SellerJpaRepository sellerJpaRepository;
    private final SellerService sellerService;

    //상품 등록
    @Transactional
    public Long createProduct(Long memberId, Long storeId, ProductCreateRequest request) {
        Store store = storeJpaRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("상점이 존재하지 않습니다."));

        //검증 로직(로그인 한 사람이 이 상품을 올릴 상점 주인이 맞는지?
        validateStoreOwner(memberId, store);

        //상품 생성
        Product product = request.toEntity(store);
        return productJpaRepository.save(product).getProductId();
    }

    //상품 수정
    @Transactional
    public void updateProduct(Long memberId, Long productId, UpdateProductRequest request) {
        Product product = productJpaRepository.findById(productId)
                .orElseThrow( () -> new IllegalArgumentException(productId + "상품이 존재하지 않습니다."));

        validateProductOwner(memberId, product);

        product.update(
                request.productName(),
                request.productPrice(),
                request.productStock(),
                product.getStore(),
                request.imageUrl(),
                product.getCategory()
        );
    }

    //상품 상세조회(단건)
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        Product product = productJpaRepository.findById(productId)
                .orElseThrow( () -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        return ProductResponse.from(product);
    }

    //특정 가게의 상품 목록 조회 (상점 페이지)
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductByStoreId(Long storeId) {
        return productJpaRepository.findByStoreId(storeId).stream()
                .map(ProductResponse::from)
                .toList();
    }

    //전체 상품목록 조회
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productJpaRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    //상품 삭제
    @Transactional
    public void deleteProduct(Long memberId, Long productId) {
        Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(productId + " 상품이 존재하지 않습니다."));

        //상품을 삭제할 권한이 있는 회원인지 검증
        validateProductOwner(memberId, product);
        productJpaRepository.delete(product);
    }

    // (상점 주인 확인용) 검증 로직
    private void validateStoreOwner(Long memberId, Store store) {
        if (!store.getSeller().getMember().getId().equals(memberId)) {
            throw new IllegalStateException("본인의 상점에만 상품을 등록할 수 있습니다.");
        }
    }

    // (상품 주인 확인용) 검증로직
    private void validateProductOwner(Long memberId, Product product) {
        Long ownerId = product.getStore().getSeller().getMember().getId();
        if (!ownerId.equals(memberId)) {
            throw new IllegalStateException("본인의 상품만 관리할 수 있습니다.");
        }
    }
}
