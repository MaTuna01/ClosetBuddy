package io.codebuddy.closetbuddy.domain.orders.model.entity;

import io.codebuddy.closetbuddy.domain.orders.exception.OrderErrorCode;
import io.codebuddy.closetbuddy.domain.orders.exception.OrderException;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;
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

    @Column(name = "order_count")
    private Integer orderCount;

    @Column(name = "order_price")
    private Long orderPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Transient
    private String productName; // 상품 이름 가져오기

    @Transient
    private Long productPrice; // 상품 가격 가져오기

    @Transient
    private Long storeName; // 가게 이름 가져오기

    public static OrderItem createOrderItem(Product product, Long productPrice, Integer orderCount) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setOrderCount(orderCount);
        orderItem.setOrderPrice(productPrice);

        // 주문 수량만큼 재고 감소
        orderItem.removeStock(orderCount);

        return orderItem;
    }

    // 주문 수량만큼 재고를 지우는 로직
    public void removeStock(Integer orderCount) {
        Integer count = product.getProductStock();
        Integer totalCount = count - orderCount;

        if (totalCount < 0) {
            throw new OrderException(OrderErrorCode.OUT_OF_STOCK);
        }

        // 상품 재고를 totalCount만큼 제거합니다.
        product.setProductStock(totalCount);
    }

    // 주문 총 가격 구하기
    public Long getTotalPrice() {
        return getOrderPrice() * getOrderCount();
    }

    protected void setOrder(Order order) {
        this.order = order;
    }

}
