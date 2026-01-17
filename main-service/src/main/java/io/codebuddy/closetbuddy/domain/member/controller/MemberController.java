package io.codebuddy.closetbuddy.domain.member.controller;

import io.codebuddy.closetbuddy.domain.member.model.dto.MemberResponse;
import io.codebuddy.closetbuddy.domain.member.model.dto.MemberUpdateRequest;
import io.codebuddy.closetbuddy.domain.member.model.dto.SellerRegisterRequest;
import io.codebuddy.closetbuddy.domain.member.service.MemberService;
import io.codebuddy.closetbuddy.domain.common.security.auth.MemberDetails;
import io.codebuddy.closetbuddy.domain.sellers.model.dto.SellerUpsertRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Member", description = "내 정보 관리 및 판매자 권한 부여 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberCommandService;

    //회원 정보 조회
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 오류)", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> me(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails principal) {
        return ResponseEntity.ok(memberCommandService.getMe(principal.getId()));
    }

    //회원 정보 수정
    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 닉네임, 프로필 등을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = MemberResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 수정 요청 데이터", content = @Content)
    })
    @PatchMapping("/me")
    public ResponseEntity<MemberResponse> updateMe(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails principal,
                                                   @RequestBody MemberUpdateRequest req) {
        return ResponseEntity.ok(memberCommandService.updateMe(principal.getId(), req));
    }

    //회원 탈퇴
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "탈퇴 성공 (No Content)"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails principal) {
        memberCommandService.deleteMe(principal.getId());
        return ResponseEntity.noContent().build();
    }

    //판매자 권한 변경
    @Operation(
            summary = "판매자 권한 부여 (Role Upgrade)",
            description = "일반 사용자(MEMBER)에게 판매자(SELLER) 권한을 부여합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "권한 변경 성공",
                    content = @Content
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 변경 거부")
    })
    @PostMapping("/me/seller")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> registerSeller(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails principal,
                                                         @RequestBody @Valid SellerUpsertRequest request) {

        Long sellerId = memberCommandService.registerSeller(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sellerId);
    }
}
