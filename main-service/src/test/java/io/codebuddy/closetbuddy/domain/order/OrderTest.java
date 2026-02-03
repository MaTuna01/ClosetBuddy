package io.codebuddy.closetbuddy.domain.order;


import io.codebuddy.closetbuddy.domain.orders.feign.OrderProductClient;
import io.codebuddy.closetbuddy.domain.orders.feign.OrderProductResponse;
import io.codebuddy.closetbuddy.domain.orders.model.dto.request.OrderCreateRequestDto;
import io.codebuddy.closetbuddy.domain.orders.model.dto.request.OrderItemCreateRequestDto;
import io.codebuddy.closetbuddy.domain.orders.repository.OrderRepository;
import io.codebuddy.closetbuddy.domain.orders.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class OrderTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProductClient orderProductClient;

    @Mock
    private OrderItemCreateRequestDto orderItemCreateRequestDto;


    @Test
    @DisplayName("일반 주문 생성 성공 - 성공하면 주문 아이디를 반환합니다.")
    void success_createOrder(){
        Long memberId = 1L;
        Long orderId = 2L;
        Long productId = 3L;
        Integer orderCount = 4;


        // given
        OrderCreateRequestDto request = new OrderCreateRequestDto(
                List.of(new OrderItemCreateRequestDto(productId, orderCount))
        );

        OrderProductResponse response =  new OrderProductResponse(
                productId,
                "나이키",
                "반바지",
                50000L
        );

        given(orderProductClient.getProduct(productId)).willReturn(response);

        // when
//        orderService.createOrder(memberId, request);

        // then
    }


}
