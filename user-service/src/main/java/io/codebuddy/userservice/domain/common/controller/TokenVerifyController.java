package io.codebuddy.userservice.domain.common.controller;

import io.codebuddy.userservice.domain.common.app.JwtTokenProvider;
import io.codebuddy.userservice.domain.common.model.dto.TokenBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class TokenVerifyController {

    private final JwtTokenProvider jwtTokenProvider;

    public record VerifyResponse(Long userId, String role) {}

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