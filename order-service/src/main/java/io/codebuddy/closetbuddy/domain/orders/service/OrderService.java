package io.codebuddy.closetbuddy.domain.orders.service;


import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartDeleteRequest;
import io.codebuddy.closetbuddy.domain.orders.kafka.OrderEventProducer;
import io.codebuddy.closetbuddy.domain.orders.model.dto.response.*;
import io.codebuddy.closetbuddy.event.PaymentRollbackRequest;
import io.codebuddy.closetbuddy.event.StockCheckRequest;
import io.codebuddy.closetbuddy.event.StockItem;
import io.codebuddy.closetbuddy.event.StockRollbackRequest;
import lombok.RequiredArgsConstructor;
import io.codebuddy.closetbuddy.domain.carts.exception.CartErrorCode;
import io.codebuddy.closetbuddy.domain.carts.exception.CartException;
import io.codebuddy.closetbuddy.domain.common.feign.dto.CartProductResponse;
import io.codebuddy.closetbuddy.domain.carts.model.dto.response.CartGetResponseDto;
import io.codebuddy.closetbuddy.domain.carts.service.CartService;
import io.codebuddy.closetbuddy.domain.common.feign.CatalogServiceClient;
import io.codebuddy.closetbuddy.domain.orders.exception.OrderErrorCode;
import io.codebuddy.closetbuddy.domain.orders.exception.OrderException;
import io.codebuddy.closetbuddy.domain.common.feign.dto.OrderProductResponse;
import io.codebuddy.closetbuddy.domain.orders.model.dto.request.OrderCreateRequestDto;
import io.codebuddy.closetbuddy.domain.orders.model.dto.request.OrderItemCreateRequestDto;
import io.codebuddy.closetbuddy.domain.orders.model.entity.Order;
import io.codebuddy.closetbuddy.domain.orders.model.entity.OrderItem;
import io.codebuddy.closetbuddy.domain.orders.repository.OrderRepository;
import io.codebuddy.closetbuddy.global.config.enumfile.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CatalogServiceClient catalogServiceClient;
    private final OrderEventProducer orderEventProducer;

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
        List<StockItem> stockItems = new ArrayList<>();

        // 요청 받은 Dto의 orderItems 를 꺼내 주문 생성 Dto에 옮겨 담습니다.
        for (OrderItemCreateRequestDto itemDto : requestDto.orderItems()) {

            // FeignClient로 상품 정보를 조회합니다.
            OrderProductResponse response = catalogServiceClient.getOrderProductInfo(itemDto.productId());

            // 주문 목록 생성
            orderItems.add(OrderItem.createOrderItem(
                    itemDto.productId(), // 상품 아이디
                    response.productName(), // 상품 이름
                    response.sellerId(), // 판매자 아이디
                    response.sellerName(), // 판매자 이름
                    response.storeId(), // 상점 아이디
                    response.storeName(), // 상점 이름
                    response.productPrice(), // 상품 가격
                    itemDto.orderCount() // 주문 수량
            ));

            // 재고 확인을 위한 StockItem 생성
            stockItems.add(new StockItem(
                    itemDto.productId(),
                    itemDto.orderCount()
            ));
        }

        // 새로운 주문 객체에 orderItem을 저장합니다.
        Order order = Order.createOrder(memberId, orderItems);
        orderRepository.save(order);

        // 재고 확인 요청 이벤트 발행
        StockCheckRequest stockCheckRequest = new StockCheckRequest(
                order.getOrderId(),
                memberId,
                stockItems
        );

        orderEventProducer.sendStockCheckRequest(stockCheckRequest);

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
        // 삭제할 장바구니 아이템 ID를 담을 리스트를 생성합니다.
        List<Long> cartItemIds = new ArrayList<>();

        // cartList에 있는 상품들을 주문 목록에 옮겨 담습니다.
        for (CartGetResponseDto cartDto : cartList) {

            // Feign Client로 cartDto에 있는 하나의 상품의 상품 ID를 조회합니다.
            CartProductResponse response = catalogServiceClient.getCartProductInfo(cartDto.productId());

            // 상품 목록을 추가합니다.
            // 상품 ID, 상품 이름, 상품 가격, 가게 이름, 장바구니에 담겨있는 수량을 가져옵니다.
            orderItems.add(OrderItem.createOrderItem(
                    cartDto.productId(), // 상품 아이디
                    response.productName(), // 상품 이름
                    response.sellerId(), // 판매자 아이디
                    response.sellerName(), // 판매자 이름
                    response.storeId(), // 가게 아이디
                    response.storeName(), // 가게 이름
                    response.productPrice(), // 상품 가격
                    cartDto.cartCount() // 상품 개수
            ));

            // cartItemId를 담습니다.
            cartItemIds.add(cartDto.cartItemId());
        }

        // 새로운 주문 객체를 만들어 회원 정보와 함께 주문 내역을 생성합니다.
        Order order = Order.createOrder(memberId, orderItems);
        orderRepository.save(order);

        // 주문이 생성되었다면, 장바구니에 있는 주문한 내역을 조회하여 삭제합니다.
        CartDeleteRequest request = new CartDeleteRequest(cartItemIds);

        // 정상적으로 생성이 되면 장바구니에 있는 상품을 삭제합니다.
        // 생성이 된 주문 내역을 장바구니에 있는 상품 내역과 비교해서 상품 내역을 삭제해줍니다.
        cartService.deleteCartItem(memberId, request);

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

                    List<OrderItemDto> itemDtos = orderItems.stream()
                            .map(item -> new OrderItemDto(
                                    item.getProductId(),
                                    item.getStoreName(),
                                    item.getProductName(),
                                    item.getOrderPrice(),
                                    item.getOrderCount()
                            )).toList();

                    return new OrderResponseDto(
                            o.getOrderId(),
                            itemDtos,
                            orderItems.stream().mapToLong(OrderItem::getTotalPrice).sum()
                    );
                }).toList();
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
                order.getOrderId(), // 주문 번호
                order.getCreatedAt(), // 생성일
                order.getOrderStatus(), // 주문 상태
                order.getOrderAmount(), // 주문 수량
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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        OrderStatus currentStatus = order.getOrderStatus();
        order.changeStatus(OrderStatus.CANCELED);

        // 상태별 보상 트랜잭션
        switch (currentStatus) {
            case PAID, COMPLETED -> {
                // 결제 + 재고 모두 롤백
                orderEventProducer.sendPaymentRollback(
                        new PaymentRollbackRequest(orderId, memberId)
                );
                List<StockItem> stockItems = order.getOrderItem().stream()
                        .map(orderItem -> new StockItem(orderItem.getProductId(), orderItem.getOrderCount()))
                        .toList();
                orderEventProducer.sendStockRollback(
                        new StockRollbackRequest(orderId, stockItems)
                );
            }
            case STOCK_CONFIRMED -> {
                // 재고만 롤백
                List<StockItem> stockItems = order.getOrderItem().stream()
                        .map(orderItem -> new StockItem(orderItem.getProductId(), orderItem.getOrderCount()))
                        .toList();
                orderEventProducer.sendStockRollback(
                        new StockRollbackRequest(orderId, stockItems)
                );
            }
            case CREATED -> {
            }
            default -> throw new OrderException(OrderErrorCode.CANCEL_NOT_ALLOWED);
        }
    }

}
