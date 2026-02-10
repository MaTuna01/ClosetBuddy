package io.codebuddy.closetbuddy.domain.orders.model.entity;

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
    private Long orderPrice; // 주문 당시 가격

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "seller_name", nullable = false)
    private String sellerName;

    @Builder
    public OrderItem(Long productId, String productName, Long storeId, String storeName, Long sellerId, String sellerName, Long orderPrice, Integer orderCount) {
        this.productId = productId;
        this.productName = productName;
        this.storeId = storeId;
        this.storeName = storeName;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.orderPrice = orderPrice;
        this.orderCount = orderCount;
    }

    public static OrderItem createOrderItem(Long productId, String productName, Long storeId, String storeName, Long sellerId, String sellerName, Long orderPrice, Integer orderCount) {
        return OrderItem.builder()
                .productId(productId)
                .productName(productName)
                .storeId(storeId)
                .storeName(storeName)
                .sellerId(sellerId)
                .sellerName(sellerName)
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
