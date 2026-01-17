package io.codebuddy.closetbuddy.domain.orders.entity;

import io.codebuddy.closetbuddy.domain.orders.exception.OutOfStockException;
import io.codebuddy.closetbuddy.domain.products.model.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @Column(name = "order_count")
    private Integer orderCount;

    @Column(name = "order_price")
    private Long orderPrice;

    @ManyToOne
    @JoinColumn(name = "orders_id")
    private Order order;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id")
    private Product product;

    private String productName; // 상품 이름 가져오기
    private Long productPrice; // 상품 가격 가져오기
    private Long storeName; // 가게 이름 가져오기

    public static OrderItem createOrderItem(Product product, Long productPrice, Integer orderCount) {

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setProductPrice(productPrice);
        orderItem.setOrderCount(orderCount);

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
        product.setProductStock(totalCount);
    }


    // 주문 총 가격 구하기
    public Long getTotalPrice() {
        Long totalPrice = orderPrice * orderCount;
        return totalPrice;
    }

    protected void setOrder(Order order) {
        this.order = order;
    }

}
