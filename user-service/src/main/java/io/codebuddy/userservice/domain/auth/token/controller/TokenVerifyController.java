package io.codebuddy.userservice.domain.auth.token.controller;

import io.codebuddy.userservice.domain.auth.token.app.JwtTokenProvider;
import io.codebuddy.userservice.domain.auth.token.dto.TokenBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Auth Token", description = "JWT 토큰 검증 및 사용자 정보 조회 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class TokenVerifyController {

    private final JwtTokenProvider jwtTokenProvider;

    @Schema(description = "JWT 토큰 검증 성공 응답")
    public record VerifyResponse(
            @Schema(description = "사용자 ID", example = "user1234")
            Long memberId,

            @Schema(description = "사용자 역할", example = "MEMBER")
            String role
    ) {}

    @Operation(
            summary = "JWT 토큰 검증",
            description = """
        **Filter 체계로 동작**
        1. JwtAuthenticationFilter → 토큰 검증
        2. 성공 → 컨트롤러 실행
        3. 실패 → JwtExceptionFilter → 401 JSON
        
        """
    )
    @ApiResponses({
            // 성공 (컨트롤러 도달)
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 정상 → 사용자 정보 반환"
            ),

            // JwtExceptionFilter 처리 (4가지 케이스)
            @ApiResponse(
                    responseCode = "401",
                    description = "토큰 만료 (ExpiredJwtException)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                {
                  "code": "TOKEN_EXPIRED",
                  "message": "토큰이 만료되었습니다. 토큰을 재발급 받으세요."
                }
                """
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "토큰 위조 (SignatureException)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                {
                  "code": "TOKEN_INVALID_SIGNATURE",
                  "message": "유효하지 않은 토큰 서명입니다."
                }
                """
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "토큰 형식 오류 (MalformedJwtException)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                {
                  "code": "TOKEN_MALFORMED",
                  "message": "토큰 형식이 올바르지 않습니다."
                }
                """
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "기타 JWT 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                {
                  "code": "TOKEN_ERROR",
                  "message": "토큰 처리 중 오류가 발생했습니다."
                }
                """
                            )
                    )
            )
    })
    @PostMapping("/verify")
    public ResponseEntity<VerifyResponse> verify(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        // 토큰 추출
        String token = extractBearerToken(authHeader);

        // 토큰이 아예 없는 경우만 컨트롤러에서 401 예외처리
        if (token == null) {
            return ResponseEntity.status(401).build();
        }

        // 토큰 파싱 검증 포함
        // 여기서 만료/위조 등의 문제가 있으면 'JwtTokenProvider'가 예외를 throw
        // -> 컨트롤러 밖으로 예외 전파 -> 'JwtExceptionFilter'가 예외 잡아서 JSON 응답 생성
        TokenBody body = jwtTokenProvider.parseJwt(token);
        //정상적으로 문제 없이 파싱되면 검증 response 반환

        return ResponseEntity.ok(new VerifyResponse(body.getMemberId(), body.getRole().name()));
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}