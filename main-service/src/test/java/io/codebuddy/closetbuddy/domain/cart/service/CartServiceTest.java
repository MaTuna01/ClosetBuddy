package io.codebuddy.closetbuddy.domain.cart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.codebuddy.closetbuddy.domain.carts.ProductClient;
import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartCreateRequestDto;
import io.codebuddy.closetbuddy.domain.carts.model.entity.Cart;
import io.codebuddy.closetbuddy.domain.carts.repository.CartRepository;
import io.codebuddy.closetbuddy.domain.carts.service.CartService;
import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static io.codebuddy.closetbuddy.domain.catalog.products.model.dto.Category.PANTS;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @InjectMocks
    CartService cartService;

    @Mock
    private ProductClient productClient;

    @Mock
    private CartRepository cartRepository;

    @Mock
    ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private final Long memberId = 1L;

    @Test
    @DisplayName("장바구니에 없는 새 상품을 담을 때 - 성공")
    void service_createCart_newProduct_success() {

        // test 체크 포인트
        // 장바구니에 해당 상품이 없을 때 새롭게 cartItem이 생성되는지

        Cart cart = Cart.createCart(memberId);


        // given
        Long memberId = 1L;
        Long productId = 2L;
        CartCreateRequestDto createReq = new CartCreateRequestDto(1L, 5);
        ProductResponse productResponse = new ProductResponse(3L, "검정치마", 50000L, 100, PANTS, "치마 전문점");

        ReflectionTestUtils.setField(cart, "id", 10L);


    }

    @Test
    @DisplayName("장바구니에 존재하는 상품을 담을 때 - 성공")
    void service_updateCart() {

        // test 체크 포인트
        // 장바구니에 이미 상품이 있을 때 수량만 증가하는지

    }

}
