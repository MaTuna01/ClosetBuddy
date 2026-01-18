package io.codebuddy.userservice.domain.oauth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
redirect-url을 임시로 지정하기 위해 만든 컨트롤러(test를 위함)
baseUrl을 http://localhost:8082/login-success 로 설정
 */
@RestController
public class OAuthTestController {
    @GetMapping("/login-success")
    public String success(@RequestParam String access) {
        return "로그인 성공! 발급된 토큰: " + access;
    }
}
