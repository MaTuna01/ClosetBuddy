package io.codebuddy.closetbuddy.domain.carts.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.codebuddy.closetbuddy.domain.carts.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal/carts")
@RequiredArgsConstructor
public class CartInternalController {

    private final CartService cartService;

    @PostMapping("/{memberId}")
    public ResponseEntity<Void> createMemberCart(@PathVariable Long memberId) {
        log.info("내부 API - 회원 장바구니 생성 요청 - member id {}", memberId);
        cartService.createCart(memberId);
        log.info("내부 API - 회원 장바구니 생성 성공 - member id {}",  memberId);
        return ResponseEntity.ok().build();
    }
}
