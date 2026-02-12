package io.codebuddy.closetbuddy.domain.orders;

import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartDeleteRequest;
import io.codebuddy.closetbuddy.domain.carts.model.dto.response.CartGetResponseDto;
import io.codebuddy.closetbuddy.domain.carts.service.CartService;
import io.codebuddy.closetbuddy.domain.common.feign.CatalogServiceClient;
import io.codebuddy.closetbuddy.domain.common.feign.dto.CartProductResponse;
import io.codebuddy.closetbuddy.domain.common.feign.dto.OrderProductResponse;
import io.codebuddy.closetbuddy.domain.orders.exception.OrderErrorCode;
import io.codebuddy.closetbuddy.domain.orders.exception.OrderException;
import io.codebuddy.closetbuddy.domain.orders.model.dto.request.OrderCreateRequestDto;
import io.codebuddy.closetbuddy.domain.orders.model.dto.request.OrderItemCreateRequestDto;
import io.codebuddy.closetbuddy.domain.orders.model.dto.response.OrderDetailResponseDto;
import io.codebuddy.closetbuddy.domain.orders.model.dto.response.OrderItemDto;
import io.codebuddy.closetbuddy.domain.orders.model.dto.response.OrderResponseDto;
import io.codebuddy.closetbuddy.domain.orders.model.entity.Order;
import io.codebuddy.closetbuddy.domain.orders.model.entity.OrderItem;
import io.codebuddy.closetbuddy.domain.orders.repository.OrderRepository;
import io.codebuddy.closetbuddy.domain.orders.service.OrderService;
import io.codebuddy.closetbuddy.global.config.enumfile.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@ExtendWith(SpringExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private CartService cartService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CatalogServiceClient catalogServiceClient;


    @Test
    @DisplayName("주문 생성 성공 테스트")
    void success_createOrder(){

        Long memberId = 1L;
        Long productId = 2L;
        Integer orderCount = 3;
        Long expectedOrderId = 1L;

        OrderItemCreateRequestDto itemDto = new OrderItemCreateRequestDto(productId, orderCount);
        OrderCreateRequestDto requestDto = new OrderCreateRequestDto(List.of(itemDto));

        OrderProductResponse productResponse = new OrderProductResponse(
                productId, "네모바지", 1L,
                "판매자 1", 1L, "뉴발란스", 1000L
        );

        given(catalogServiceClient.getOrderProductInfo(productId))
                .willReturn(productResponse);

        // save 가 호출되면 저장된 객체의 Id를 10L로 세팅해서 돌려준다고 가정한다.
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "orderId", 1L);
            return order;
        });

        // when
        Long resultOrderId = orderService.createOrder(memberId, requestDto);

        // then
        // 반환된 주문 ID가 1L 인지 확인한다.
        assertThat(resultOrderId).isEqualTo(expectedOrderId);

        // repository로 넘어간 시점의 Order 객체를 가져온다.
        ArgumentCaptor<Order> orderArgumentCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderArgumentCaptor.capture());

        Order savedOrder = orderArgumentCaptor.getValue();

        // savedOrderId가 멤버 아이디와 같은지
        assertThat(savedOrder.getOrderId()).isEqualTo(memberId);
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);

        // saveItem에 있는 값과 예상 값 검증
        OrderItem savedItem = savedOrder.getOrderItem().get(0);
        assertThat(savedItem.getProductId()).isEqualTo(productId);
        assertThat(savedItem.getProductName()).isEqualTo("네모바지");
        assertThat(savedItem.getOrderPrice()).isEqualTo(1000L);
        assertThat(savedItem.getTotalPrice()).isEqualTo(3000L);

        log.info("주문 생성 테스트 성공");
    }


    @Test
    @DisplayName("장바구니 -> 주문 생성 성공 테스트")
    void success_updateOrder(){

        // given
        Long memberId = 1L;
        Long productId = 2L;

        CartGetResponseDto cartDto = new CartGetResponseDto(1L, productId, "삼각형 티셔츠",
                1000L, 2, "아디다스", "triangle_shirts");

        List<CartGetResponseDto> cartDtoList = List.of(cartDto);

        CartProductResponse productResponse = new CartProductResponse(
                productId, "네모바지", 10L, "판매자 1",
                1L, "뉴발란스", 10000L, "square.png");

        given(cartService.getCartList(memberId)).willReturn(cartDtoList);
        given(catalogServiceClient.getCartProductInfo(productId)).willReturn(productResponse);

        // when
        orderService.createOrderFromCart(memberId);

        // then
        // 주문 repository가 1번 동작했는지 검증
        verify(orderRepository, times(1)).save(any(Order.class));
        // 장바구니 -> 주문 생성 후 deleteCartItem이 실제로 이루어졌는지
        verify(cartService, times(1)).deleteCartItem(any(), any());

        log.info("장바구니의 상품 주문 테스트 완료");
    }

    @Test
    @DisplayName("주문 목록 조회 성공 테스트")
    void success_getCart(){
        // given
        Long memberId = 1L;

        // 객체 생성
        Order order = Order.createOrder(memberId, new ArrayList<>());

        // findAllByMemberId를 했을 때, order 리스트를 반환
        given(orderRepository.findAllByMemberId(memberId)).willReturn(List.of(order));
        ReflectionTestUtils.setField(order, "orderId", 1L);

        // when
        List<OrderResponseDto> result = orderService.getOrder(memberId);

        // then
        assertThat(result.size()).isEqualTo(1);
        log.info("주문 목록 조회 테스트 완료");
    }

    @Test
    @DisplayName("주문 목록 조회 실패 - 주문 내역이 없을 경우")
    void failed_getCart(){

        // given
        Long memberId = 1L;

        // emptyList를 반환한다고 설정
        given(orderRepository.findAllByMemberId(memberId)).willReturn(Collections.emptyList());

        // when, then
        // errorCode 검증
        assertThatThrownBy(() -> orderService.getOrder(memberId)).isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_NOT_FOUND);

        log.info("주문 목록 조회에 실패 테스트 완료");
    }

    @Test
    @DisplayName("주문 상세 조회 성공 테스트")
    void success_getDetailOrder(){

        // 상세 조회값을 넣어 실제로 그 값이 들어가는지 확인하는 로직 필요
        // given
        Long memberId = 1L;
        Long orderId = 100L;

        OrderItem item = OrderItem.createOrderItem(2L, "테스트상품", 1L, "판매자", 1L, "가게", 5000L, 2);

        Order order = Order.createOrder(memberId, List.of(item));
        ReflectionTestUtils.setField(order, "orderId", orderId);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        ReflectionTestUtils.setField(order, "orderId", orderId);

        // when
        OrderDetailResponseDto responseDto = orderService.getDetailOrder(memberId, orderId);

        // then
        assertThat(responseDto.orderId()).isEqualTo(orderId);
        assertThat(responseDto.orderStatus()).isEqualTo(OrderStatus.CREATED);

        assertThat(responseDto.orderItems().get(0).productName());
        assertThat(responseDto.orderItems().get(0).orderPrice());

        log.info("주문 상세 조회 테스트 완료");
    }

    @Test
    @DisplayName("주문 취소 성공 테스트")
    void success_cancelOrder(){

        // given
        Long memberId = 1L;
        Long orderId = 100L;
        Order order = Order.createOrder(memberId, new ArrayList<>());

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // when
        orderService.cancelOrder(memberId, orderId);

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
        log.info("주문 취소를 성공 테스트 완료");
    }

    @Test
    @DisplayName("주문 취소 실패 - 주문이 존재하지 않을 때")
    void failed_cancelOrder(){

        // given
        Long memberId = 1L;
        Long orderId = 100L;
        given(orderRepository.findById(orderId)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> orderService.cancelOrder(memberId, orderId))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_NOT_FOUND);

        log.info("주문 취소 실패 테스트 완료");
    }
}
