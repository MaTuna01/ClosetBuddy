package io.codebuddy.closetbuddy.domain.carts.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "cart")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {

    @Id
    @Column(name = "cart_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    // 생성자
    @Builder
    public static Cart createCart(Long memberId) {
        Cart cart = new Cart();
        cart.memberId = memberId;
        return cart;
    }
}
