package org.dev.orderservice.domain.orders.service;


import lombok.RequiredArgsConstructor;
import org.dev.orderservice.domain.carts.exception.CartErrorCode;
import org.dev.orderservice.domain.carts.exception.CartException;
import org.dev.orderservice.domain.carts.model.dto.response.CartProductResponse;
import org.dev.orderservice.domain.carts.model.dto.response.CartGetResponseDto;
import org.dev.orderservice.domain.carts.service.CartService;
import org.dev.orderservice.domain.common.feign.OrderServiceClient;
import org.dev.orderservice.domain.orders.exception.OrderErrorCode;
import org.dev.orderservice.domain.orders.exception.OrderException;
import org.dev.orderservice.domain.orders.model.dto.response.OrderProductResponse;
import org.dev.orderservice.domain.orders.model.dto.request.OrderCreateRequestDto;
import org.dev.orderservice.domain.orders.model.dto.request.OrderItemCreateRequestDto;
import org.dev.orderservice.domain.orders.model.dto.response.OrderDetailResponseDto;
import org.dev.orderservice.domain.orders.model.dto.response.OrderItemDto;
import org.dev.orderservice.domain.orders.model.dto.response.OrderResponseDto;
import org.dev.orderservice.domain.orders.model.entity.Order;
import org.dev.orderservice.domain.orders.model.entity.OrderItem;
import org.dev.orderservice.domain.orders.repository.OrderRepository;
import org.dev.orderservice.global.config.enumfile.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrderServiceClient orderServiceClient;

    /**
     * 주문을 생성합니다.
     *
     * @param memberId
     * @param requestDto
     * @return
     */
    @Transactional
    public Long createOrder(Long memberId, OrderCreateRequestDto requestDto) {

        // 회원이 존재하지 않을 경우
        if (memberId == null) {
            throw new OrderException(OrderErrorCode.NOT_MEMBER);
        }

        List<OrderItem> orderItems = new ArrayList<>();

        // 요청 받은 Dto의 orderItems 를 꺼내 주문 생성 Dto에 옮겨 담습니다.
        for (OrderItemCreateRequestDto itemDto : requestDto.orderItems()) {

            // FeignClient로 상품 정보를 조회합니다.
            OrderProductResponse response = orderServiceClient.getProductWithOrder(itemDto.productId());

            // 주문 목록 생성 -> 스냅샷을 저장합니다.
            // 생성하기 위해 받았던 Dto에서 상품 아이디와 주문 수량을 받아오고,
            // 상점 이름, 상품 가격, 상품 이름은 FeignClient를 통해 불러와 orderItem 에 추가해줍니다.
            orderItems.add(OrderItem.createOrderItem(
                    itemDto.productId(),
                    response.productName(),
                    response.storeName(),
                    response.productPrice(),
                    itemDto.orderCount()
            ));
        }

        // 새로운 주문 객체에 orderItem을 저장합니다.
        // 주문이라는 틀 안에 주문 목록들이 들어갑니다.
        Order order = Order.createOrder(memberId, orderItems);
        orderRepository.save(order);

        // 생성된 주문 아이디를 반환합니다.
        return order.getOrderId();
    }

    /**
     * 장바구니에 담은 물품을 주문할 수 있도록 합니다.
     *
     * @param memberId
     * @return
     */
    @Transactional
    public Long createOrderFromCart(Long memberId) {

        // 로그인한 사용자 정보로 장바구니 목록을 조회합니다.
        List<CartGetResponseDto> cartList = cartService.getCartList(memberId);

        // 만약 장바구니가 비어있는 경우 장바구니가 존재하지 않음을 예외처리합니다.
        if (cartList.isEmpty()) {
            throw new CartException(CartErrorCode.CART_NOT_FOUND);
        }

        // 주문 리스트를 생성합니다.
        List<OrderItem> orderItems = new ArrayList<>();

        // cartList에 있는 상품들을 주문 목록에 옮겨 담습니다.
        for (CartGetResponseDto cartDto : cartList) {

            // Feign Client로 cartDto에 있는 하나의 상품의 상품 ID를 조회합니다.
            CartProductResponse response = orderServiceClient.getProductWithCart(cartDto.productId());

            // 상품 목록을 추가합니다.
            // 상품 ID, 상품 이름, 상품 가격, 가게 이름, 장바구니에 담겨있는 수량을 가져옵니다.
            orderItems.add(OrderItem.createOrderItem(
                    cartDto.productId(),
                    response.productName(),
                    response.storeName(),
                    response.productPrice(),
                    cartDto.cartCount()
            ));
        }

        // 새로운 주문 객체를 만들어 회원 정보와 함께 주문 내역을 생성합니다.
        Order order = Order.createOrder(memberId, orderItems);
        orderRepository.save(order);

        // 정상적으로 생성이 되면 장바구니에 있는 상품을 삭제합니다.
        for (CartGetResponseDto cartDto : cartList) {
            cartService.deleteCartItem(memberId, cartDto.cartItemId());
        }

        // 주문 ID를 반환합니다.
        return order.getOrderId();
    }


    /**
     * 회원의 모든 주문을 불러옵니다.
     * memberId로 주문 조회 -> 컨트롤러에서 리스트로 반환
     *
     * @param memberId
     * @return
     */
    public List<OrderResponseDto> getOrder(Long memberId) {

        // 회원의 주문 내역이 존재하는지 확인합니다.
        List<Order> order = orderRepository.findAllByMemberId(memberId);

        // 만약 회원의 주문 내역이 없다면 주문 내역이 없다는 예외를 반환합니다.
        if (order.isEmpty()) {
            throw new OrderException(OrderErrorCode.ORDER_NOT_FOUND);
        }
        return order.stream()
                .map(o -> {
                    List<OrderItem> orderItems = o.getOrderItem();

                    return new OrderResponseDto(o.getOrderId(),
                            orderItems.stream()
                                    .map(oi -> oi.getProductName())
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
     *
     * @param memberId
     * @param orderId
     * @return
     */
    public OrderDetailResponseDto getDetailOrder(Long memberId, Long orderId) {

        // 주문 내역이 없는 경우, 주문이 존재하지 않는다는 예외를 반환합니다.
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // 주문이 사용자의 주문인지 확인합니다.

        // OrderItemDto에 주문 상품들의 상세 내역을 넣어 반환해줍니다.
        List<OrderItemDto> itemDto = order.getOrderItem().stream()
                .map(oi -> new OrderItemDto(
                        oi.getProductId(),
                        oi.getStoreName(),
                        oi.getProductName(),
                        oi.getOrderPrice(),
                        oi.getOrderCount()
                )).toList();

        // 최종적으로 orderDetailResponseDto 로 변환해줍니다.
        // 주문 상세 목록 Dto를 생성해줍니다.
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
     *
     * @param memberId
     * @param orderId
     */
    @Transactional
    public void cancelOrder(Long memberId, Long orderId) {

        // 주문 ID를 통해 삭제할 주문 내역이 없으면 주문을 찾을 수 없다는 예외를 반환해줍니다.
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        order.changeStatus(OrderStatus.CANCELED);
    }


}
