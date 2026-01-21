package io.codebuddy.closetbuddy.domain.products.model.entity;

import io.codebuddy.closetbuddy.domain.products.model.dto.Category;
import io.codebuddy.closetbuddy.domain.stores.model.entity.Store;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    //PK는 productId
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "product_price", nullable = false)
    private Long productPrice;

    @Column(name = "product_stock", nullable = false)
    private int  productStock;

    //store 엔티티의 store_id를 fk로 가짐
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private Category category;

    @Builder
    public Product(Long productId, String productName, Long productPrice, int productStock, Store store, String imageUrl, Category category) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productStock = productStock;
        this.store = store;
        this.imageUrl = imageUrl;
        //카테고리는 분류가 안되어 있을 수 있음(null 허용)
        this.category = category;
    }

    //update 비지니스 로직을 담은 메서드
    public void update(String productName, Long productPrice, int productStock, Store store, String imageUrl, Category category) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.productStock = productStock;
        this.store = store;
        this.imageUrl = imageUrl;
        this.category = category;
    }
}
