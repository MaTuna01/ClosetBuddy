package io.codebuddy.closetbuddy.domain.cart.controller;

import io.codebuddy.closetbuddy.domain.carts.controller.CartController;
import io.codebuddy.closetbuddy.domain.carts.model.dto.request.CartCreateRequestDto;
import io.codebuddy.closetbuddy.domain.carts.model.dto.response.CartGetResponseDto;
import io.codebuddy.closetbuddy.domain.carts.service.CartService;
import io.codebuddy.closetbuddy.domain.common.web.CurrentUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @InjectMocks
    private CartController cartController;

    @Mock
    CartService cartService;

    private MockMvc mockMvc;

    // 테스트가 실행되기 전에 한번 실행
    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
    }

    @Test
    @DisplayName("컨트롤러 테스트 - 장바구니 생성 성공시 cartItemId를 반환한다.")
    void controller_getCartSuccess() throws Exception {

        // given
        // userId = 1L, productId = 1L, cartItemId 기댓값 40L
        Long productId = 1L;
        Long exCartItemId = 40L;

        // 현재 로그인한 사용자의 정보를 임의로 입력한다.
        CurrentUserInfo userInfo = new CurrentUserInfo("1", "SELLER");
        CartCreateRequestDto cartCreateRequestDto = new CartCreateRequestDto(productId, 5);

        // 서비스에 첫 번째 인자로 숫자 1이 들어오고 뒤에 아무 DTO나 들어온다면 40L을 리턴하도록 한다.
        given(cartService.createCart(eq(1L), any()))
                .willReturn(exCartItemId);

        // when
        ResponseEntity<Long> response = cartController.createCart(userInfo, cartCreateRequestDto);

        // then
        // 상태 코드가 201 created 인지 확인해준다.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 바디에 들어있는 ID가 40L 인지 확인한다.
        assertThat(response.getBody()).isEqualTo(exCartItemId);

        // 검증
        verify(cartService).createCart(eq(1L), any());
    }


    @Test
    @DisplayName("컨트롤러 테스트 - 장바구니 조회 테스트")
    void controller_getCart() throws Exception {

        Long memberId = 1L;

        // given
        // 유저 정보를 받아온다.
        CurrentUserInfo userInfo = new CurrentUserInfo("1", "SELLER");

        // when
        // 컨트롤러에 userInfo를 주입한다.
        ResponseEntity<List<CartGetResponseDto>> cartList = cartController.getCart(userInfo);

        // then
        // userInfo를 주입하였을 때 상태코드가 ok라면 성공
        assertThat(cartList.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(cartService).getCartList((memberId));
    }


    @Test
    @DisplayName("컨트롤러 테스트 - 장바구니 수정 테스트")
    void controller_updateCart() throws Exception {
        Long memberId = 1L;

    }
}