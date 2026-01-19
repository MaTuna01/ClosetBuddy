package io.codebuddy.closetbuddy.domain.orders.service;

import io.codebuddy.closetbuddy.domain.common.repository.MemberRepository;
import io.codebuddy.closetbuddy.domain.common.model.entity.Member;
import io.codebuddy.closetbuddy.domain.orders.dto.response.OrderDetailResponseDto;
import io.codebuddy.closetbuddy.domain.orders.dto.response.OrderItemCreateRequestDto;
import io.codebuddy.closetbuddy.domain.orders.dto.response.OrderItemDto;
import io.codebuddy.closetbuddy.domain.orders.dto.response.OrderResponseDto;
import io.codebuddy.closetbuddy.domain.orders.entity.OrderItem;
import io.codebuddy.closetbuddy.domain.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.products.repository.ProductJpaRepository;
import io.codebuddy.closetbuddy.global.config.enumfile.OrderStatus;
import io.codebuddy.closetbuddy.domain.orders.dto.request.OrderCreateRequestDto;
import io.codebuddy.closetbuddy.domain.orders.entity.Order;
import io.codebuddy.closetbuddy.domain.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final ProductJpaRepository productJpaRepository;
    private final OrderRepository orderRepository;

    /**
     * 주문을 생성합니다.
     * @param memberId
     * @param requestDto
     * @return
     */
    @Transactional
    public Long createOrder(Long memberId, OrderCreateRequestDto requestDto) {

        /**
         * 회원 객체를 생성하여 memberId를 통해 회원이 존재하는지 조회합니다.
         * 존재하지 않으면 예외를 반환합니다.
         */
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemCreateRequestDto itemDto : requestDto.orderItems()) {
            Product product = productJpaRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
            OrderItem orderItem = OrderItem.createOrderItem(
                    product, product.getProductPrice(), itemDto.orderCount()
            );
            orderItems.add(orderItem);
        }

        /**
         * 새로운 객체를 만들어서 orderItem을 저장합니다.
         */
        Order order = Order.createOrder(memberId, orderItems);
        orderRepository.save(order);

        /**
         * 생성된 주문 아이디를 반환합니다.
         */
        return order.getOrderId();
    }

    /**
     * 회원의 모든 주문을 불러옵니다.
     * memberId로 주문 조회 -> 컨트롤러에서 리스트로 반환
     * @param memberId
     * @return
     */
    public List<OrderResponseDto> getOrder(Long memberId) {
        /**
         * 주문한 내역에서 회원이 존재하는지 확인합니다.
         */
        List<Order> order = orderRepository.findAllByMemberId(memberId);

        /**
         * 만약 회원의 주문 내역이 없다면 예외를 반환합니다.
         */
        if(order.isEmpty()) {
            throw new IllegalArgumentException("주문 내역이 없습니다.");
        }

        return order.stream()
                .map(o -> {
                    List<OrderItem> orderItems = o.getOrderItem();
                    return new OrderResponseDto(o.getOrderId(),
                            orderItems.stream()
                                    .map(oi -> oi.getProduct().getProductName())
                                    .toList()
                            ,
                            orderItems.stream()
                                    .mapToLong(OrderItem::getTotalPrice)
                                    .sum());
                })
                .toList();
    }


    /**
     * 주문 하나의 상세 내용을 조회합니다.
     * @param orderId
     * @return
     */
    public OrderDetailResponseDto getDetailOrder(Long memberId, Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 없습니다."));

        if(!order.getMemberId().equals(memberId)) {
            throw new AccessDeniedException("본인의 주문만 조회할 수 있습니다.");
        }

        /**
         * OrderItemDto에 주문 상품들의 상세 내역을 넣어 반환해줍니다.
         */
        List<OrderItemDto> itemDto = order.getOrderItem().stream()
                .map(oi -> new OrderItemDto(
                        oi.getProduct().getProductId(),
                        oi.getProduct().getStore().getStoreName(),
                        oi.getProduct().getProductName(),
                        oi.getOrderCount(),
                        oi.getOrderPrice()
                )).toList();

        /**
         * 최종적으로 orderDetailResponseDto 로 변환해줍니다.
         */
        return new OrderDetailResponseDto(
                order.getOrderId(),
                order.getCreatedAt(),
                order.getOrderStatus(),
                order.getOrderAmount(),
                itemDto
        );
    }

    /**
     * 주문을 취소합니다.
     * @param orderId 주문을 삭제하지만 실질적으로 상태값만 바뀜
     *                -> 정산할때 주문 내역을 취소 포함해서 보여줘야하기 때문에
     */
    @Transactional
    public void cancelOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문한 내역이 아직 없습니다."));


        if(!order.getMemberId().equals(memberId)) {
            throw new AccessDeniedException("주문자만 취소가 가능합니다.");
        }

        /**
         * AccessDeniedException을 통해 회원이 아닌 경우
         * 예외처리를 진행합니다.
         */
        if(order.getOrderStatus()==OrderStatus.CANCELED) {
            throw new AccessDeniedException("이미 취소된 주문입니다.");
        }
        order.changeStatus(OrderStatus.CANCELED);
    }
}
