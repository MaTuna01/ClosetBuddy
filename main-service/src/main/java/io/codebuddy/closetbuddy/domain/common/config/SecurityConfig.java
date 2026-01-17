package io.codebuddy.closetbuddy.domain.common.config;


import io.codebuddy.closetbuddy.domain.form.login.security.config.MemberAuthFailureHandler;
import io.codebuddy.closetbuddy.domain.form.login.security.config.MemberAuthSuccessHandler;
import io.codebuddy.closetbuddy.domain.form.logout.config.ApiLogoutSuccessHandler;
import io.codebuddy.closetbuddy.domain.form.logout.config.JwtLogoutHandler;
import io.codebuddy.closetbuddy.domain.oauth.config.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

//Spring Security의 보안 필터 체인을 정의하는 설정 클래스
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    private final MemberAuthSuccessHandler memberAuthSuccessHandler;
    private final MemberAuthFailureHandler memberAuthFailureHandler;
    private final JwtLogoutHandler jwtLogoutHandler;

    private final ApiLogoutSuccessHandler apiLogoutSuccessHandler;

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //1. SecurityFilterChain 빈으로 OAuth2 로그인과 JWT 필터 등록.

    /* Spring Security 설정을 통해 기존 인증 방식을 비활성화하고 API 엔드포인트에 대해 JWT 기반의 상태
    비저장 보안을 사용하는 OAuth2 로그인을 설정*/
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {



        return http
                .securityMatcher("/oauth2/**","/login/oauth2/**", "/api/**","/api/v1/**")
                .authorizeHttpRequests((request) -> request
                        .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/authc/**").permitAll()
                        .requestMatchers("/api/v1/payments/**").hasAnyAuthority("MEMBER", "SELLER")
                        .requestMatchers("/api/v1/account/**").hasAnyAuthority("MEMBER", "SELLER")
                        .requestMatchers("/api/v1/catalog/sellers/**").hasAnyAuthority("SELLER")
                        .requestMatchers("/api/v1/catalog/stores/**").hasAnyAuthority("SELLER")
                        .requestMatchers("/api/v1/catalog/products/**").hasAnyAuthority("GUEST")
                        .requestMatchers("/api/v1/orders").hasAnyAuthority("MEMBER")
                        .requestMatchers("/api/v1/carts").hasAnyAuthority("MEMBER")
                        .requestMatchers("/api/v1/auth/refresh").hasAnyAuthority("MEMBER","SELLER")
                        .anyRequest().authenticated() //그외에는 인증 필요
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .cors( cors -> cors.disable())
                .httpBasic(httpB -> httpB.disable())

                //로그인
                .formLogin(form -> form
                        .loginProcessingUrl("/api/v1/auth/login")
                        .successHandler(memberAuthSuccessHandler)
                        .failureHandler(memberAuthFailureHandler)
                        .usernameParameter("memberId")
                        .passwordParameter("password")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout") // 로그아웃 처리 URL
                        .logoutSuccessUrl("/api/v1/auth/loginForm?logout=1") // 로그아웃 성공 후 이동페이지
                        .deleteCookies("JSESSIONID") // 로그아웃 후 쿠키 삭제
                        .addLogoutHandler(jwtLogoutHandler)
                        .logoutSuccessHandler(apiLogoutSuccessHandler)
                )

                //OAuth2 로그인 활성화
                .oauth2Login(oauth -> {
                    oauth.successHandler(oAuth2SuccessHandler);
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();// 필터 체인 빌드

    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
