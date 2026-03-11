package io.codebuddy.userservice.domain.member.controller;


import io.codebuddy.userservice.domain.auth.token.security.principal.MemberDetails;
import io.codebuddy.userservice.domain.member.dto.MemberResponse;
import io.codebuddy.userservice.domain.member.dto.MemberUpdateRequest;
import io.codebuddy.userservice.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member Profile", description = "인증된 사용자의 개인정보 관리 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberCommandService;

    //회원 정보 조회
    @Operation(
            summary = "내 정보 조회",
            description = """
            **JwtAuthenticationFilter → @AuthenticationPrincipal**
            
            **Flow:**
            1. Bearer 토큰 검증 (Filter)
            2. SecurityContext에서 MemberDetails 추출
            3. memberCommandService.getMe(id) → MemberResponse
            
            **보안:** MEMBER/SELLER 권한 필요 (SecurityConfig)
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원 정보 반환",
                    content = @Content(
                            schema = @Schema(implementation = MemberResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                      "id": 12345,
                      "memberId": "user@example.com",
                      "name": "홍길동",
                      "email": "user@example.com",
                      "address": "서울시 강남구...",
                      "phone": "010-1234-5678",
                      "role": "MEMBER"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JwtExceptionFilter (토큰 문제)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                      "code": "TOKEN_EXPIRED",
                      "message": "토큰이 만료되었습니다."
                    }
                    """
                            )
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> me(@AuthenticationPrincipal MemberDetails principal) {
        return ResponseEntity.ok(memberCommandService.getMe(principal.getId()));
    }

    //회원 정보 수정
    @Operation(
            summary = "내 정보 수정",
            description = """
            **부분 업데이트 지원 + @Valid 검증**
            
            **검증 규칙 (GlobalExceptionHandler):**
            - name: 1자 이상
            - email: 올바른 이메일 형식
            - phone: 010-0000-0000 형식
            - address: 필수
            
            **Flow:**
            1. JwtAuthenticationFilter 검증
            2. @Valid → GlobalExceptionHandler (400)
            3. memberCommandService.updateMe()
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정된 회원 정보 반환",
                    content = @Content(schema = @Schema(implementation = MemberResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "GlobalExceptionHandler.INVALID_INPUT_VALUE",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                      "code": "INVALID_INPUT_VALUE",
                      "message": "입력값이 유효하지 않습니다.",
                      "errors": [
                        {
                          "field": "phone",
                          "value": "010-123",
                          "reason": "올바른 전화번호 형식(010-0000-0000)이 아닙니다."
                        }
                      ]
                    }
                    """
                            )
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/me")
    public ResponseEntity<MemberResponse> updateMe(@AuthenticationPrincipal MemberDetails principal,
                                                   @Valid @RequestBody MemberUpdateRequest req) {
        return ResponseEntity.ok(memberCommandService.updateMe(principal.getId(), req));
    }

    //회원 탈퇴
    @Operation(
            summary = "회원 탈퇴",
            description = """
            **소프트 딜리트 예상**
            
            **보안:**
            - MEMBER/SELLER 권한 필요
            - 탈퇴 후 JWT 무효화 (Redis Refresh Token 삭제)
            
            **응답:** 204 No Content (본문 없음)
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "회원 탈퇴 완료"),
            @ApiResponse(
                    responseCode = "401",
                    description = "JwtExceptionFilter (토큰 문제)"
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal MemberDetails principal) {
        memberCommandService.deleteMe(principal.getId());
        return ResponseEntity.noContent().build();
    }

}
