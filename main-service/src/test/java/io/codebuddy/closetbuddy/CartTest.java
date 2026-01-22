package io.codebuddy.closetbuddy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.codebuddy.closetbuddy.domain.carts.controller.CartController;
import io.codebuddy.closetbuddy.domain.carts.dto.request.CartCreateRequestDto;
import io.codebuddy.closetbuddy.domain.carts.service.CartService;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUser;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUserInfo;
import io.codebuddy.closetbuddy.domain.orders.dto.response.CartGetResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CartService cartService;

    /**
     * @CurrentUser 어노테이션을 처리하기 위한 ArgumentResolver를
     * 테스트 컨텍스트에 등록합니다.
     */
    @TestConfiguration
    static class TestWebConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(CurrentUser.class)
                            && parameter.getParameterType().equals(CurrentUserInfo.class);
                }

                @Override
                public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                    // 가짜 유저 정보 리턴 (userId: "1")
                    return new CurrentUserInfo("1", "ROLE_USER");
                }
            });
        }
    }

    // =========================
    // 1. CREATE (장바구니 생성)
    // =========================
    @Test
    @DisplayName("장바구니 생성 성공")
    void createCart_success() throws Exception {
        // Given
        CartCreateRequestDto request = new CartCreateRequestDto(1L, 2);

        given(cartService.createCart(eq(1L), any(CartCreateRequestDto.class)))
                .willReturn(10L);

        // When & Then
        mockMvc.perform(post("/api/v1/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print()) // 테스트 로그 출력
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(content().string("10")); // Body 확인

        verify(cartService).createCart(eq(1L), any(CartCreateRequestDto.class));
    }

    // =========================
    // 2. READ (장바구니 목록 조회)
    // =========================
    @Test
    @DisplayName("장바구니 조회 성공")
    void getCart_success() throws Exception {
        // Given
        List<CartGetResponseDto> responseList = List.of();

        given(cartService.getCartList(1L))
                .willReturn(responseList);

        // When & Then
        mockMvc.perform(get("/api/v1/carts")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$").isArray()); // 배열인지 확인

        verify(cartService).getCartList(1L);
    }

    // =========================
    // 3. UPDATE (수량 수정)
    // =========================
    @Test
    @DisplayName("장바구니 수량 수정 성공")
    void updateCartItem_success() throws Exception {
        // Given
        Long cartItemId = 10L;
        int newCount = 5;

        // 반환값이 void인 경우 willDoNothing 사용
        willDoNothing().given(cartService).updateCart(1L, cartItemId, newCount);

        // When & Then
        mockMvc.perform(patch("/api/v1/carts/items/{cartItemId}", cartItemId)
                        .param("cartCount", String.valueOf(newCount))) // RequestParam 처리
                .andDo(print())
                .andExpect(status().isOk()); // Controller에서 ok()를 반환하므로 200

        verify(cartService).updateCart(1L, cartItemId, newCount);
    }

    @Test
    @DisplayName("장바구니 수량 수정 실패 - 수량 부족")
    void updateCartItem_fail_invalid_count() throws Exception {
        // Given
        Long cartItemId = 10L;
        int invalidCount = 0; // 1 미만

        // When & Then (서비스 호출 전에 컨트롤러 검증에서 걸려야 함)
        mockMvc.perform(patch("/api/v1/carts/items/{cartItemId}", cartItemId)
                        .param("cartCount", String.valueOf(invalidCount)))
                .andDo(print())
                .andExpect(status().isBadRequest()) // 400 Bad Request 예상 (ExceptionAdvice 처리에 따라 다를 수 있음) or InternalServerError
                .andExpect(result -> {
                    // 예외가 실제로 발생했는지 확인 (IllegalArgumentException)
                    if (!(result.getResolvedException() instanceof IllegalArgumentException)) {
                        throw new AssertionError("IllegalArgumentException이 발생해야 합니다.");
                    }
                });
    }

    // =========================
    // 4. DELETE (삭제)
    // =========================
    @Test
    @DisplayName("장바구니 삭제 성공")
    void deleteCartItem_success() throws Exception {
        // Given
        Long cartItemId = 10L;

        willDoNothing().given(cartService).deleteCartItem(1L, cartItemId);

        // When & Then
        mockMvc.perform(delete("/api/v1/carts/items/{cartItemId}", cartItemId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(cartService).deleteCartItem(1L, cartItemId);
    }
}