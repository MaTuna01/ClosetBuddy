package io.codebuddy.closetbuddy.domain.orders.entity;

import io.codebuddy.closetbuddy.domain.orders.exception.OutOfStockException;
import io.codebuddy.closetbuddy.domain.products.model.entity.Product;
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

    // cascade를 제거했습니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

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
            throw new OutOfStockException("상품의 재고가 부족합니다." + "현재 재고 수량: " + totalCount);
        }

        // 상품 재고를 totalCount만큼 제거합니다.
        product.setProductStock(totalCount);
    }

    // 주문 총 가격 구하기
    public Long getTotalPrice() {
        return getOrderPrice() + getOrderCount();
    }

    protected void setOrder(Order order) {
        this.order = order;
    }

}
