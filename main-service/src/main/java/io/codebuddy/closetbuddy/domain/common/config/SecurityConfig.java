package io.codebuddy.closetbuddy.domain.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //CSRF 보안 끄기
                .csrf(AbstractHttpConfigurer::disable)

                //모든 요청에 대해 인증 없이 통과 허용 (Gateway가 인증을 신뢰)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )

                //로그인 폼, HTTP Basic 인증 무시
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
