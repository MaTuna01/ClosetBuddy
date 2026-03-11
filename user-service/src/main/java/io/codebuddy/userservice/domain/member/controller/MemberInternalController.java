package io.codebuddy.userservice.domain.member.controller;

import io.codebuddy.userservice.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 내부 서비스 간 통신용 API Controller
 * Gateway를 거치지 않는 서비스 간 직접 통신에 사용
 * Security에서 인증 없이 접근 가능하도록 설정됨
 */
@Slf4j
@RestController
@RequestMapping("/internal/members")
@RequiredArgsConstructor
public class MemberInternalController {

    private final MemberService memberService;

    /**
     * 판매자 역할 부여 (main-service에서 호출)
     * 
     * @param memberId 대상 회원 ID
     */
    @PostMapping("/{memberId}/seller")
    public ResponseEntity<Void> grantSellerRole(@PathVariable Long memberId) {
        log.info("내부 API - 판매자 역할 부여 요청 - memberId: {}", memberId);
        memberService.registerSeller(memberId);
        log.info("내부 API - 판매자 역할 부여 완료 - memberId: {}", memberId);
        return ResponseEntity.ok().build();
    }

    /**
     * 판매자 역할 해제 (main-service에서 호출)
     * 
     * @param memberId 대상 회원 ID
     */
    @DeleteMapping("/{memberId}/seller")
    public ResponseEntity<Void> revokeSellerRole(@PathVariable Long memberId) {
        log.info("내부 API - 판매자 역할 해제 요청 - memberId: {}", memberId);
        memberService.revokeSeller(memberId);
        log.info("내부 API - 판매자 역할 해제 완료 - memberId: {}", memberId);
        return ResponseEntity.ok().build();
    }
}
