package io.codebuddy.closetbuddy.domain.oauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
redirect-url을 임시로 지정하기 위해 만든 컨트롤러(test를 위함)
baseUrl을 http://localhost:8082/login-success 로 설정
 */
@Tag(name = "OAuth Test", description = "OAuth2 인증 테스트용 콜백 API")
@RestController
public class OAuthTestController {

    @Operation(
            summary = "로그인 성공 테스트용 리다이렉트",
            description = "OAuth2 인증 완료 후 리다이렉트되어 발급된 Access 토큰을 화면에 표시합니다. (개발/테스트용)"
    )
    @GetMapping("/login-success")
    public String success(
            @Parameter(description = "OAuth2 인증을 통해 발급된 Access 토큰", required = true)
            @RequestParam String access) {
        return "로그인 성공! 발급된 토큰: " + access;
    }
}
