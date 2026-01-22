package io.codebuddy.closetbuddy.domain.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.codebuddy.closetbuddy.domain.orders.controller.OrderController;
import io.codebuddy.closetbuddy.domain.orders.model.dto.request.OrderCreateRequestDto;
import io.codebuddy.closetbuddy.domain.orders.model.dto.response.OrderDetailResponseDto;
import io.codebuddy.closetbuddy.domain.orders.model.dto.response.OrderItemCreateRequestDto;
import io.codebuddy.closetbuddy.domain.orders.model.dto.response.OrderItemDto;
import io.codebuddy.closetbuddy.domain.orders.model.dto.response.OrderResponseDto;
import io.codebuddy.closetbuddy.domain.orders.service.OrderService;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUser;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUserInfo;
import io.codebuddy.closetbuddy.global.config.enumfile.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(OrderTest.TestWebConfig.class)
class OrderTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    OrderService orderService;

    /**
     * 테스트용 @CurrentUser ArgumentResolver
     */
    @TestConfiguration
    static class TestWebConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new TestCurrentUserArgumentResolver());
        }
    }

    static class TestCurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(CurrentUser.class)
                    && parameter.getParameterType().equals(CurrentUserInfo.class);
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            // Security 없이도 강제로 사용자 주입
            return new CurrentUserInfo("1", "USER");
        }
    }

    // =========================
    // CREATE
    // =========================
    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_success() throws Exception {

        OrderCreateRequestDto request =
                new OrderCreateRequestDto(
                        List.of(
                                new OrderItemCreateRequestDto(1L, 2),
                                new OrderItemCreateRequestDto(2L, 1)
                        )
                );

        given(orderService.createOrder(eq(1L), any()))
                .willReturn(100L);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));

        verify(orderService).createOrder(eq(1L), any());
    }

    // =========================
    // READ - LIST
    // =========================
    @Test
    @DisplayName("주문 목록 조회 성공")
    void getOrderList_success() throws Exception {

        given(orderService.getOrder(1L))
                .willReturn(List.of(
                        new OrderResponseDto(1L, List.of("티셔츠", "청바지"), 50000L),
                        new OrderResponseDto(2L, List.of("운동화"), 120000L)
                ));

        mockMvc.perform(get("/api/v1/orders/orderList"))
                .andExpect(status().isOk());

        verify(orderService).getOrder(1L);
    }

    // =========================
    // READ - DETAIL
    // =========================
    @Test
    @DisplayName("주문 상세 조회 성공")
    void getOrderDetail_success() throws Exception {

        given(orderService.getDetailOrder(1L, 10L))
                .willReturn(
                        new OrderDetailResponseDto(
                                10L,
                                LocalDateTime.now(),
                                OrderStatus.CREATED,
                                170000L,
                                List.of(
                                        new OrderItemDto(1L, "나이키", "티셔츠", 2, 50000L),
                                        new OrderItemDto(2L, "나이키", "청바지", 1, 70000L)
                                )
                        )
                );

        mockMvc.perform(get("/api/v1/orders/10"))
                .andExpect(status().isOk());

        verify(orderService).getDetailOrder(1L, 10L);
    }

    // =========================
    // UPDATE (CANCEL)
    // =========================
    @Test
    @DisplayName("주문 취소 성공")
    void cancelOrder_success() throws Exception {

        mockMvc.perform(patch("/api/v1/orders/10/status"))
                .andExpect(status().isOk());

        verify(orderService).cancelOrder(1L, 10L);
    }

    // =========================
    // CREATE FROM CART
    // =========================
    @Test
    @DisplayName("장바구니 주문 생성 성공")
    void createOrderFromCart_success() throws Exception {

        mockMvc.perform(post("/api/v1/orders/cart/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));

        verify(orderService).createOrderFromCart(1L);
    }
}
