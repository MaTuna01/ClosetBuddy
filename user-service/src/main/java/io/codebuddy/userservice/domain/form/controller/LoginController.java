package io.codebuddy.userservice.domain.form.controller;


import io.codebuddy.userservice.domain.common.app.JwtTokenProvider;
import io.codebuddy.userservice.domain.common.model.dto.UserReqDTO;
import io.codebuddy.userservice.domain.common.security.auth.MemberDetails;
import io.codebuddy.userservice.domain.form.signup.service.SignService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;


import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/api/v1")
public class LoginController {

    private final SignService signService;

    @Autowired
    private AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    //회원가입
    @PostMapping("/authc")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> create(@RequestBody UserReqDTO userReqDTO) {
        signService.create(userReqDTO);
        return ResponseEntity.status(
                HttpStatus.CREATED
        ).body("회원가입 성공");
    }

    //로그인
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(HttpSession session, @RequestBody UserReqDTO userReqDTO) {

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userReqDTO.getMemberId(), userReqDTO.getPassword())
            );

            MemberDetails userDetails = (MemberDetails) auth.getPrincipal(); //UserDetails를 구현한 객체가 가지고 있는 정보들을 가지고 옴
            session.setAttribute("loggedInUser", userDetails.getUsername());
            session.setAttribute("loginErrorMessage","");

            Map<String, String> response = new HashMap<>();
            response.put("message", "Login success");
            response.put("userId", userDetails.getUsername());
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "잘못된 아이디 또는 비밀번호입니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (DisabledException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "비활성 계정입니다.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (AuthenticationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "인증 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}
