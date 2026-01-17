package io.codebuddy.closetbuddy.domain.orders.entity;

import io.codebuddy.closetbuddy.domain.common.model.entity.Member;
import io.codebuddy.closetbuddy.global.config.enumfile.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Column(name = "order_amount")
    private Long orderAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private List<OrderItem> orderItem = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    // 주문하면 주문 상품(orderItem) 값이 들어간다.
    public static Order createOrder(Member member, List<OrderItem> orderItems) {

        // 주문 객체 생성
        Order order = new Order();

        // 주문 회원 설정
        order.SetMember(member);
        order.orderStatus = OrderStatus.CREATED;
        order.createdAt = LocalDateTime.now();

        // 주문 상품 리스트를 생성한다.
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        return order;
    }

    // 회원 생성용
    private void SetMember(Member member) {
        this.member = member;
    }

    // 주문 아이템 추가 로직
    public void addOrderItem(OrderItem orderItem) {
        this.orderItem.add(orderItem);
        orderItem.setOrder(this);
    }

    // 주문 삭제용 -> 실제로 삭제 하지 않으니 상태값만 Cancelled로 변경한다.
    public void changeStatus(OrderStatus newStatus) {
        this.orderStatus = newStatus;
    }
}
