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
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "cart_count", nullable = false)
    private Integer cartCount;

    @Column(name = "cart_price", nullable = false)
    private Long cartPrice;


    @Builder
    public CartItem Builder(Cart cart, Long productId, Integer cartCount, Long cartPrice) {
        this.cart = cart;
        this.productId = productId;
        this.cartCount = cartCount;
        this.cartPrice = cartPrice;

        return this;
    }

    /**
     *  cart 수량을 변경합니다.
     */
    public void updateCount(Integer cartCount) {
        this.cartCount += cartCount;
    }

}
