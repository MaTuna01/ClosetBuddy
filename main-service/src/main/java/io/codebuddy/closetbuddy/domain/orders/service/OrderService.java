package io.codebuddy.closetbuddy.domain.orders.service;

import io.codebuddy.closetbuddy.domain.common.repository.MemberRepository;
import io.codebuddy.closetbuddy.domain.common.model.entity.Member;
import io.codebuddy.closetbuddy.domain.orders.dto.response.OrderDetailResponseDto;
import io.codebuddy.closetbuddy.domain.orders.dto.response.OrderItemDto;
import io.codebuddy.closetbuddy.domain.orders.dto.response.OrderResponseDto;
import io.codebuddy.closetbuddy.domain.orders.entity.OrderItem;
import io.codebuddy.closetbuddy.domain.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.products.repository.ProductJpaRepository;
import io.codebuddy.closetbuddy.global.config.enumfile.OrderStatus;
import io.codebuddy.closetbuddy.domain.orders.dto.request.OrderRequestDto;
import io.codebuddy.closetbuddy.domain.orders.entity.Order;
import io.codebuddy.closetbuddy.domain.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final ProductJpaRepository productJpaRepository;
    private final OrderRepository orderRepository;

    /**
     * 주문을 생성합니다.
     * memberId 회원이 존재하는지 조회하고 주문을 생성합니다.
     * @param memberId
     * @param requestDto
     * @return
     */
    @Transactional
    public Long createOrder(Long memberId, OrderRequestDto requestDto) {

        /**
         * 회원 객체 생성하여 memberId를 통해 회원이 존재하는지 조회합니다.
         * 조회하지 않는다면 예외를 반환합니다.
         */
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        /**
         * 주문 상품들이 들어갈 리스트를 생성합니다.
         */
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDto itemDto : requestDto.orderItemDtoList()) {

            /**
             * 주문 리스트에 상품이 존재하는지 확인합니다.
             */
            Product product = productJpaRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

            OrderItem orderItem = OrderItem.createOrderItem(product, product.getProductPrice(), itemDto.orderCount());

            orderItems.add(orderItem);
        }

        Order order = Order.createOrder(member, orderItems);
        orderRepository.save(order);


        /**
         * 생성된 주문에 status를 CREATED로 바꾸어줍니다.
         */

        order.changeStatus(OrderStatus.CREATED);

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
            new IllegalArgumentException("주문 내역이 없습니다.");
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
                                    .mapToLong(oi -> oi.getProduct().getProductPrice())
                                    .sum());
                })
                .toList();
    }


    /**
     * 주문 하나의 상세 내용을 조회합니다.
     * @param orderId
     * @return
     */
    public OrderDetailResponseDto getDetailOrder(Long orderId) {

        /**
         * 주문을 조회하고 없으면 예외를 발생시킵니다.
         */
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 없습니다."));

        /**
         * OrderItemDto에 주문 상품들의 상세 내역을 넣어 반환해줍니다.
         */
        List<OrderItemDto> itemDto = order.getOrderItem().stream()
                .map(orderItem -> new OrderItemDto(
                        orderItem.getId(),
                        orderItem.getProduct().getStore().getStoreName(),
                        orderItem.getProductName(),
                        orderItem.getOrderCount(),
                        orderItem.getOrderPrice()
                )).toList();


        /**
         * 모든 가게 이름을 끌어옵니다. 가게 이름은 주문 -> 주문 상품 -> 가게 이름으로 연결되어있습니다.
         */
        String storeName = order.getOrderItem().stream()
                .map(orderItem -> orderItem.getProduct().getStore().getStoreName())
                .distinct()
                .collect(Collectors.joining(", "));


        /**
         * 최종적으로 orderDetailResponseDto 로 변환해줍니다.
         */
        return new OrderDetailResponseDto(
                order.getOrderId(),
                storeName,
                order.getCreatedAt(),
                itemDto
        );

    }

    /**
     * 주문을 취소합니다.
     * @param orderId 주문을 삭제하지만 실질적으로 상태값만 바뀜
     *                -> 정산할때 주문 내역을 취소 포함해서 보여줘야하기 때문에
     */
    @Transactional
    public void cancelledOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문한 내역이 아직 없습니다."));


        /**
         * AccessDeniedException을 통해 회원이 아닌 경우
         * 예외처리를 진행합니다.
         */
        if(!orderId.equals(memberId)) {
            throw new AccessDeniedException("주문자만 주문 취소가 가능합니다.");
        }

        order.changeStatus(OrderStatus.CANCELED);
    }
}
