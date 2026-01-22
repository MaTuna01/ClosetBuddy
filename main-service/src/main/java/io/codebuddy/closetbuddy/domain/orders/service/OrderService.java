package io.codebuddy.closetbuddy.domain.orders.service;

import io.codebuddy.closetbuddy.domain.carts.exception.CartErrorCode;
import io.codebuddy.closetbuddy.domain.carts.exception.CartException;
import io.codebuddy.closetbuddy.domain.carts.model.dto.response.CartGetResponseDto;
import io.codebuddy.closetbuddy.domain.carts.service.CartService;
import io.codebuddy.closetbuddy.domain.orders.exception.OrderErrorCode;
import io.codebuddy.closetbuddy.domain.orders.exception.OrderException;
import io.codebuddy.closetbuddy.domain.orders.model.dto.response.OrderDetailResponseDto;
import io.codebuddy.closetbuddy.domain.orders.model.dto.response.OrderItemCreateRequestDto;
import io.codebuddy.closetbuddy.domain.orders.model.dto.response.OrderItemDto;
import io.codebuddy.closetbuddy.domain.orders.model.dto.response.OrderResponseDto;
import io.codebuddy.closetbuddy.domain.orders.model.entity.OrderItem;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductJpaRepository;
import io.codebuddy.closetbuddy.global.config.enumfile.OrderStatus;
import io.codebuddy.closetbuddy.domain.orders.model.dto.request.OrderCreateRequestDto;
import io.codebuddy.closetbuddy.domain.orders.model.entity.Order;
import io.codebuddy.closetbuddy.domain.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductJpaRepository productJpaRepository;
    private final OrderRepository orderRepository;
    private final CartService cartService;


    /**
     * 주문을 생성합니다.
     * @param memberId
     * @param requestDto
     * @return
     */
    @Transactional
    public Long createOrder(Long memberId, OrderCreateRequestDto requestDto) {

        if (memberId == null) {
            throw new OrderException(OrderErrorCode.NOT_MEMBER);
        }

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemCreateRequestDto itemDto : requestDto.orderItems()) {
            Product product = productJpaRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND));

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
        if (order.isEmpty()) {
            throw new OrderException(OrderErrorCode.ORDER_NOT_FOUND);
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
     * 주문 하나의 상세 내역을 조회합니다.
     * @param memberId
     * @param orderId
     * @return
     */
    public OrderDetailResponseDto getDetailOrder(Long memberId, Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
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
     * 주문을 삭제하지만 실질적으로 상태값만 바뀜
     * -> 정산할때 주문 내역을 취소 포함해서 보여줘야하기 때문에
     * @param memberId
     * @param orderId
     */
    @Transactional
    public void cancelOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        order.changeStatus(OrderStatus.CANCELED);
    }

    /**
     * 장바구니에 담은 물품을 주문할 수 있도록 합니다.
     * @param memberId
     * @return
     */
    @Transactional
    public Long createOrderFromCart(Long memberId) {

        List<CartGetResponseDto> cartList = cartService.getCartList(memberId);

        if (cartList.isEmpty()) {
            throw new CartException(CartErrorCode.CART_NOT_FOUND);
        }

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartGetResponseDto cartDto : cartList) {
            Product product = productJpaRepository.findById(cartDto.productId())
                    .orElseThrow(() -> new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND));

            OrderItem orderItem = OrderItem.createOrderItem(
                    product,
                    product.getProductPrice(),
                    cartDto.cartCount()
            );
            orderItems.add(orderItem);
        }

        Order order = Order.createOrder(memberId, orderItems);
        orderRepository.save(order);

        for (CartGetResponseDto cartDto : cartList) {
            cartService.deleteCartItem(memberId, cartDto.cartItemId());
        }

        return order.getOrderId();
    }

}
