package io.codebuddy.closetbuddy.domain.common.controller;

import io.codebuddy.closetbuddy.domain.common.app.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@Tag(name = "Auth", description = "인증 관련 API (토큰 재발급 등)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class RefreshTokenController {

    private final JwtTokenProvider jwtTokenProvider;

    /*
    클라이언트가 보내는 요청 JSON 바디를 매핑하기 위한 DTO
    {"refreshToken":"..."} 형태로 받는다.
     */
    @Schema(description = "토큰 재발급 요청 DTO")
    public record RefreshRequest(
            @Schema(description = "리프레시 토큰")
            String refreshToken) {}

    /*
    서버가 새 access 토큰만 내려주기 위한 응답 DTO
    {"accessToken":"..."} 형태로 응답
     */
    @Schema(description = "새로운 액세스 토큰 응답 DTO")
    public record AccessResponse(
            @Schema(description = "새로 발급된 액세스 토큰")
            String accessToken) {}

    @Operation(
            summary = "Access 토큰 재발급",
            description = "Access 토큰이 만료되었을 때 Refresh 토큰을 사용하여 새로운 Access 토큰을 발급 받습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재발급 성공",
            content = @Content(schema = @Schema(implementation = AccessResponse.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰",
            content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<AccessResponse> refresh(@RequestBody RefreshRequest req) {
        String newAccess = jwtTokenProvider.reissueAccessTokenByRefresh(req.refreshToken());
        return ResponseEntity.ok(new AccessResponse(newAccess));
    }
}
