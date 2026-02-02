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

    private String productName;

    @Builder
    public CartItem(Cart cart, Long productId, Integer cartCount, String productName) {
        this.cart = cart;
        this.productId = productId;
        this.cartCount = cartCount;
        this.productName = productName;
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
