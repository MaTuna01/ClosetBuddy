package io.codebuddy.userservice.domain.common.controller;

import io.codebuddy.userservice.domain.common.app.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 Access 토큰이 만료됐을 때, Refresh 토큰을 보내면 새 Access 토큰만 발급해주는 API 엔드포인트를 만든 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class RefreshTokenController {

    private final JwtTokenProvider jwtTokenProvider;

    /*
    클라이언트가 보내는 요청 JSON 바디를 매핑하기 위한 DTO
    {"refreshToken":"..."} 형태로 받는다.
     */
    public record RefreshRequest(String refreshToken) {}

    /*
    서버가 새 access 토큰만 내려주기 위한 응답 DTO
    {"accessToken":"..."} 형태로 응답
     */
    public record AccessResponse(String accessToken) {}

    @PostMapping("/refresh")
    public ResponseEntity<AccessResponse> refresh(@RequestBody RefreshRequest req) {
        String newAccess = jwtTokenProvider.reissueAccessTokenByRefresh(req.refreshToken());
        return ResponseEntity.ok(new AccessResponse(newAccess));
    }
}
