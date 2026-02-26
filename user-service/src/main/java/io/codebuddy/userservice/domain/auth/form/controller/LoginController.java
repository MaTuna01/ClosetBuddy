package io.codebuddy.userservice.domain.auth.form.controller;


import io.codebuddy.userservice.domain.auth.token.dto.LoginReqDTO;
import io.codebuddy.userservice.domain.auth.token.dto.SignReqDTO;
import io.codebuddy.userservice.domain.auth.token.security.principal.MemberDetails;
import io.codebuddy.userservice.domain.auth.form.service.LogoutService;
import io.codebuddy.userservice.domain.auth.form.service.SignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Auth(Form)", description = "폼 로그인/회원가입")
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/v1")
public class LoginController {

    private final SignService signService;

    private AuthenticationManager authenticationManager;

    private final LogoutService logoutService;


    //회원가입
    @Operation(
            summary = "회원가입",
            description = """
            **폼 기반 회원가입**
            
            **Flow:**
            1. @Valid 검증 (GlobalExceptionHandler 처리)
            2. SignService.create() → DB 저장 + BCrypt 암호화
            3. 201 Created 반환
            
            **실패 케이스:**
            - 400: GlobalExceptionHandler.INVALID_INPUT_VALUE (이메일 형식 등)
            - 409: DuplicateMemberFieldException → DUPLICATE_VALUE
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "GlobalExceptionHandler.INVALID_INPUT_VALUE",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                    {
                      "code": "INVALID_INPUT_VALUE",
                      "message": "입력값이 유효하지 않습니다.",
                      "errors": [
                        {
                          "field": "email",
                          "value": "wrong-email",
                          "reason": "올바른 이메일 형식이 아닙니다."
                        }
                      ]
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "DuplicateMemberFieldException",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                    {
                      "code": "DUPLICATE_VALUE",
                      "message": "중복된 값이 존재합니다.",
                      "errors": [
                        {
                          "field": "memberId",
                          "value": "test@example.com",
                          "reason": "이미 사용중인 이메일입니다."
                        }
                      ]
                    }
                    """
                            )
                    )
            )
    })
    @PostMapping("/authc")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> create(@Valid @RequestBody SignReqDTO signReqDTO) {
        signService.create(signReqDTO);
        return ResponseEntity.status(
                HttpStatus.CREATED
        ).body("회원가입 성공");
    }

    //로그인
    @Operation(
            summary = "폼 로그인",
            description = """
            **MemberAuthenticationProvider + CustomAuthenticationFailureHandler**
            
            **Flow:**
            1. AuthenticationManager.authenticate()
            2. MemberPrincipalDetailService.loadUserByUsername()
            3. BCryptPasswordEncoder.matches() 검증
            4. 성공 → MemberAuthSuccessHandler
            5. 실패 → CustomAuthenticationFailureHandler (401 JSON)
            
            **실제 로그인 URL: POST /api/v1/auth/login**
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "MemberAuthSuccessHandler → TokenPair 반환",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                      "accessToken": "eyJhbGciOiJIUzUxMi...",
                      "refreshToken": "eyJhbGciOiJIUzI1Ni..."
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "CustomAuthenticationFailureHandler.INVALID_CREDENTIALS",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                    {
                      "code": "INVALID_CREDENTIALS",
                      "message": "아이디 또는 비밀번호가 올바르지 않습니다."
                    }
                    """
                            )
                    )
            )
    })
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(HttpSession session, @Parameter(description = "Form Data 형식으로 입력") @ModelAttribute LoginReqDTO loginReqDTO) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginReqDTO.getMemberId(), loginReqDTO.getPassword())
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

    @Operation(
            summary = "세션 로그아웃",
            description = """
            **LogoutService.signOut() 호출**
            
            **헤더:**
            - X-USER-ID: 로그아웃할 사용자 ID
            
            **처리:**
            1. 세션 무효화
            2. 204 No Content
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 완료"),
            @ApiResponse(responseCode = "400", description = "X-USER-ID 형식 오류"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("X-USER-ID") String memberId
    ) {
        long targetId = Long.parseLong(memberId);
        logoutService.signOut(targetId);
//        logoutService.deleteRefreshToken(targetId);
        return ResponseEntity.noContent().build();
    }

}
