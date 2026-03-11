package io.codebuddy.userservice.domain.auth.token.controller;

import io.codebuddy.userservice.domain.auth.token.service.TokenRefreshService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/*
 Access 토큰이 만료됐을 때, Refresh 토큰을 보내면 새 Access 토큰만 발급해주는 API 엔드포인트를 만든 컨트롤러
 */
@Tag(name = "Auth Token", description = "JWT Refresh Token API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class RefreshTokenController {

    private final TokenRefreshService tokenRefreshService;

    /*
    클라이언트가 보내는 요청 JSON 바디를 매핑하기 위한 DTO
    {"refreshToken":"..."} 형태로 받는다.
     */
    @Schema(description = "Refresh 토큰 재발급 요청")
    public record RefreshRequest(
            @Schema(description = "Redis에 저장된 유효한 Refresh 토큰", example = "eyJhbGciOiJIUzI1Ni...")
            String refreshToken) {}

    /*
    서버가 새 access 토큰만 내려주기 위한 응답 DTO
    {"accessToken":"..."} 형태로 응답
     */
    @Schema(description = "새 Access 토큰 응답")
    public record AccessResponse(
            @Schema(description = "짧은 TTL의 새 Access 토큰", example = "eyJhbGciOiJIUzUxMi...")
            String accessToken) {}

    @Operation(
            summary = "Access 토큰 재발급",
            description = """
            **Filter 체계 적용됨**
            JwtAuthenticationFilter → JwtExceptionFilter → 컨트롤러
            
            **Refresh Flow:**
            1. Access 토큰 만료 (JwtExceptionFilter TOKEN_EXPIRED)
            2. 클라이언트가 Refresh 토큰으로 이 API 호출
            3. Redis에서 Refresh 토큰 검증
            4. 유효 → 새 Access 토큰 발급 (Refresh 토큰은 유지)
            
            **요청 예시:**
            ```json
            {
              "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            }
            ```
            """
    )
    @PostMapping("/refresh")
    public ResponseEntity<AccessResponse> refresh(@RequestBody RefreshRequest req) {
        String newAccess = tokenRefreshService.refreshAccessToken(req.refreshToken());
        return ResponseEntity.ok(new AccessResponse(newAccess));
    }
}
