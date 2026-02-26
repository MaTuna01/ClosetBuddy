package io.codebuddy.userservice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GoogleOAuth2Test extends TestSupport{

    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("✅ Google OAuth2 로그인 시작 - 302 리다이렉트")
    void google_oauth2_authorization_redirect() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection());
    }

    //인증을 하지 않고 회원 조회를 할 경우 401이 뜨는지 확인하는 테스트
    @Test
    @DisplayName("✅ 인증 없이 /api/v1/members/me 요청 → 401")
    void members_me_unauthenticated_401() throws Exception {
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized());
    }
}
