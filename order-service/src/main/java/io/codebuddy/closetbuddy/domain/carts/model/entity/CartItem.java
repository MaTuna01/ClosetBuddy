package io.codebuddy.closetbuddy.domain.carts.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "cart_count", nullable = false)
    private Integer cartCount;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_price")
    private Long productPrice;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "seller_id")
    private Long sellerId;

    @Column(name = "seller_name")
    private String sellerName;

    @Builder
    public CartItem(Cart cart, Long sellerId, String sellerName, Long productId, String productName, Long productPrice, Long storeId, String storeName, Integer cartCount) {
        this.cart = cart;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.storeId = storeId;
        this.storeName = storeName;
        this.cartCount = cartCount;
    }

    /**
     * 카트 수량 넣기 (생성할 때)
     * @param cartCount
     */
    public void addCount(Integer cartCount){
        this.cartCount += cartCount;
    }

    /**
     *  cart 수량을 변경합니다.
     */
    public void updateCount(Integer cartCount) {
        this.cartCount = cartCount;
    }

}
