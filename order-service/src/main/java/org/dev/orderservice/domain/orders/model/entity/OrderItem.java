package org.dev.orderservice.domain.orders.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount;

    @Column(name = "order_price", nullable = false)
    private Long orderPrice; // 주문 당시 가격 (스냅샷 - 주문 당시 기록을 남겨놔야하기 때문에)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName; // 상품 이름 가져오기, DB에 저장되어야하므로 @Column으로 선언합니다.

    @Column(name = "store_name", nullable = false)
    private String storeName; // 가게 이름 가져오기, DB에 저장되어야하므로 @Column으로 선언합니다.


    @Builder
    public OrderItem(Long productId, String storeName, String productName, Long orderPrice, Integer orderCount) {
        this.productId = productId;
        this.storeName = storeName;
        this.productName = productName;
        this.orderPrice = orderPrice;
        this.orderCount = orderCount;
    }

    public static OrderItem createOrderItem(Long productId, String storeName, String productName, Long orderPrice, Integer orderCount) {
        return OrderItem.builder()
                .productId(productId)
                .storeName(storeName)
                .productName(productName)
                .orderPrice(orderPrice)
                .orderCount(orderCount)
                .build();
    }

    // 주문 총 가격 구하기
    public Long getTotalPrice() {
        return getOrderPrice() * getOrderCount();
    }

    protected void setOrder(Order order) {
        this.order = order;
    }

}
