package io.codebuddy.closetbuddy.domain.carts.entity;

import io.codebuddy.closetbuddy.domain.products.model.entity.Product;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "cart_count", nullable = false)
    private Integer cartCount;

    @Column(name = "cart_price", nullable = false)
    private Long cartPrice;


    @Builder
    public CartItem(Cart cart, Product product, Integer cartCount, Long cartPrice) {
        this.cart = cart;
        this.product = product;
        this.cartCount = cartCount;
        this.cartPrice = cartPrice;
    }

    /**
     *  cart 수량을 변경합니다.
     */
    public void updateCount(Integer cartCount) {
        this.cartCount += cartCount;
    }

}
