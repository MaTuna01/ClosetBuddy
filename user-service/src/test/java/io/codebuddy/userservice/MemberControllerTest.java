package io.codebuddy.userservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.codebuddy.userservice.domain.member.controller.MemberController;
import io.codebuddy.userservice.domain.member.model.dto.MemberUpdateRequest;
import io.codebuddy.userservice.domain.member.service.MemberService;
import io.codebuddy.userservice.domain.common.security.auth.JwtAuthenticationFilter;
import io.codebuddy.userservice.domain.common.security.auth.JwtExceptionFilter;
import io.codebuddy.userservice.domain.common.security.handler.CustomAuthenticationEntryPoint;
import io.codebuddy.userservice.domain.common.security.handler.CustomAccessDeniedHandler;
import io.codebuddy.userservice.domain.form.login.security.config.CustomAuthenticationFailureHandler;
import io.codebuddy.userservice.domain.form.login.security.config.MemberAuthSuccessHandler;
import io.codebuddy.userservice.domain.oauth.config.OAuth2SuccessHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(MemberController.class)
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // SecurityConfig에 정의된 모든 의존성을 Mocking 해줘야 합니다.
    @MockitoBean private MemberService memberService;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private JwtExceptionFilter jwtExceptionFilter;
    @MockitoBean private OAuth2SuccessHandler oAuth2SuccessHandler;
    @MockitoBean private MemberAuthSuccessHandler memberAuthSuccessHandler;
    @MockitoBean private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    @MockitoBean private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    @MockitoBean private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Test
    @DisplayName("내 정보 조회 - 인증된 사용자 성공")
    @WithMockUser
    void getMe_Success() throws Exception {
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원 정보 수정 - 잘못된 이메일 형식(마침표 없음) 시 400 에러")
    @WithMockUser(authorities = "MEMBER")
    void updateMe_Success() throws Exception {
        MemberUpdateRequest request = new MemberUpdateRequest("손재현", "sonjae@naver", "010-1234-5678", "Seoul");

        mockMvc.perform(patch("/api/v1/members/me")
                        .with(csrf()) // csrf를 테스트 가독성과 안전을 위해 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("판매자 권한 등록 - 성공")
    @WithMockUser(authorities = "MEMBER")
    void registerSeller_Success() throws Exception {
        mockMvc.perform(post("/api/v1/members/me/seller")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원 탈퇴 - 성공")
    @WithMockUser(authorities = "MEMBER")
    void deleteMe_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/members/me")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
