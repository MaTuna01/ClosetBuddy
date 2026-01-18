package io.codebuddy.userservice.domain.member.controller;


import io.codebuddy.userservice.domain.common.security.auth.MemberDetails;
import io.codebuddy.userservice.domain.member.model.dto.MemberResponse;
import io.codebuddy.userservice.domain.member.model.dto.MemberUpdateRequest;
import io.codebuddy.userservice.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberCommandService;

    //회원 정보 조회
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> me(@AuthenticationPrincipal MemberDetails principal) {
        return ResponseEntity.ok(memberCommandService.getMe(principal.getId()));
    }

    //회원 정보 수정
    @PatchMapping("/me")
    public ResponseEntity<MemberResponse> updateMe(@AuthenticationPrincipal MemberDetails principal,
                                                   @RequestBody MemberUpdateRequest req) {
        return ResponseEntity.ok(memberCommandService.updateMe(principal.getId(), req));
    }

    //회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal MemberDetails principal) {
        memberCommandService.deleteMe(principal.getId());
        return ResponseEntity.noContent().build();
    }
}
