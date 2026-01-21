package io.codebuddy.userservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.codebuddy.userservice.domain.common.model.dto.Role;
import io.codebuddy.userservice.domain.common.model.dto.SignReqDTO;
import io.codebuddy.userservice.domain.common.model.entity.Member;
import io.codebuddy.userservice.domain.common.model.entity.RefreshToken;
import io.codebuddy.userservice.domain.common.repository.MemberRepository;
import io.codebuddy.userservice.domain.common.repository.RefreshTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private MemberRepository memberRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        // (선택) 과거 실행에서 남아있는 testmember가 있을 수 있으면 정리
        memberRepository.findByMemberId("testmember").ifPresent(m -> {
            refreshTokenRepository.deleteAllByMember_Id(m.getId());
            memberRepository.delete(m);
        });

        Member member = Member.builder()
                .memberId("testmember")
                .password(passwordEncoder.encode("testrequest12!"))
                .username("테스트사용자")
                .email("test@naver.com")
                .address("Seoul")
                .phone("010-1111-2222")
                .role(Role.MEMBER) // 프로젝트 enum 값에 맞게 수정
                .build();

        Member savedMember = memberRepository.saveAndFlush(member);
        this.testUserId = savedMember.getId();

        // logout 로직이 찾을 refresh token을 미리 생성(삭제하지 않을 정책이어도 테스트용으로 1개는 필요)
        refreshTokenRepository.saveAndFlush(
                RefreshToken.builder()
                        .member(savedMember)
                        .refreshToken("dummy-refresh-token")
                        .build()
        );
    }

    @Test
    @DisplayName("회원가입 - 유효성 검사 실패 시 400 에러 (이메일 형식 오류)")
    void signup_Fail_InvalidEmail() throws Exception {
        SignReqDTO request = new SignReqDTO(
                "테스트사용자", "testuser", "invalid@naver",
                "passwordtest123!", "Seoul", "010-1111-2222"
        );

        mockMvc.perform(post("/api/v1/authc")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 - 성공 시 200 OK")
    void login_Success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .param("memberId", "testmember")
                        .param("password", "testrequest12!")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그아웃 - 성공 시 204 No Content")
    @WithMockUser
    void logout_Success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf())
                        // LogoutService.signOut()는 member PK로 refresh token을 찾음
                        .header("X-USER-ID", testUserId.toString()))
                .andExpect(status().isNoContent());
    }
}
