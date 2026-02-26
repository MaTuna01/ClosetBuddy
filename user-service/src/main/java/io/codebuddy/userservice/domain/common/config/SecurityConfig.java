package io.codebuddy.userservice.domain.common.config;

import io.codebuddy.userservice.domain.auth.token.security.handler.CustomAuthenticationEntryPoint;
import io.codebuddy.userservice.domain.auth.token.security.filter.JwtExceptionFilter;
import io.codebuddy.userservice.domain.auth.token.security.filter.JwtAuthenticationFilter;
import io.codebuddy.userservice.domain.auth.token.security.handler.CustomAccessDeniedHandler;
import io.codebuddy.userservice.domain.auth.form.security.handler.CustomAuthenticationFailureHandler;
import io.codebuddy.userservice.domain.auth.form.security.handler.MemberAuthSuccessHandler;
import io.codebuddy.userservice.domain.auth.oauth.config.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsUtils;

import java.util.Collections;

//Spring Security의 보안 필터 체인을 정의하는 설정 클래스
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

        private final OAuth2SuccessHandler oAuth2SuccessHandler;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        private final JwtExceptionFilter jwtExceptionFilter; // [추가]

        private final MemberAuthSuccessHandler memberAuthSuccessHandler;
        private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

        // [추가]
        private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
        private final CustomAccessDeniedHandler customAccessDeniedHandler;

        // 1. SecurityFilterChain 빈으로 OAuth2 로그인과 JWT 필터 등록.

        /*
         * Spring Security 설정을 통해 기존 인증 방식을 비활성화하고 API 엔드포인트에 대해 JWT 기반의 상태
         * 비저장 보안을 사용하는 OAuth2 로그인을 설정
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                return http
                                .cors(cors -> cors.configurationSource(request -> {
                                        CorsConfiguration config = new CorsConfiguration();
                                        config.setAllowedOrigins(Collections.singletonList("http://localhost:8090")); // Swagger 주소
                                        config.setAllowedMethods(Collections.singletonList("*"));
                                        config.setAllowedHeaders(Collections.singletonList("*"));
                                        config.setAllowCredentials(true);
                                        return config;
                                }))
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests((request) -> request
                                                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                                                .requestMatchers( "/login-success").permitAll()
                                                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                                                .requestMatchers("/api/v1/auth/**").permitAll()
                                                .requestMatchers("/api/v1/authc/**").permitAll()
                                                // 내부 서비스 간 통신용 API - 인증 불필요
                                                .requestMatchers("/internal/**").permitAll()
                                                // Prometheus 테스트를 위해 인가, 배포 시 주석 처리된 부분 활용
                                                //.requestMatchers("/actuator/prometheus", "/actuator/health", "/actuator/info").permitAll()
                                                //.requestMatchers("/actuator/**").denyAll()
                                                .requestMatchers("/actuator/**").permitAll()


                                                // swagger 허용범위 설정
                                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                                                .requestMatchers("/closetbuddy-user/v3/api-docs").permitAll()
                                                .requestMatchers("/closetbuddy-main/v3/api-docs").permitAll()

                                                .requestMatchers("/api/v1/members/**")
                                                .hasAnyAuthority("MEMBER", "SELLER")
                                                .requestMatchers("/api/v1/payments/**")
                                                .hasAnyAuthority("MEMBER", "SELLER")
                                                .requestMatchers("/api/v1/account/**")
                                                .hasAnyAuthority("MEMBER", "SELLER")
                                                .requestMatchers("/api/v1/catalog/sellers/**").hasAnyAuthority("SELLER")
                                                .requestMatchers("/api/v1/catalog/stores/**").hasAnyAuthority("SELLER")
                                                .requestMatchers("/api/v1/catalog/products/**").hasAnyAuthority("GUEST")
                                                .requestMatchers("/api/v1/orders").hasAnyAuthority("MEMBER")
                                                .requestMatchers("/api/v1/carts").hasAnyAuthority("MEMBER")
                                                .requestMatchers("/api/v1/auth/refresh")
                                                .hasAnyAuthority("MEMBER", "SELLER")
                                                .anyRequest().authenticated() // 그외에는 인증 필요
                                )
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .httpBasic(httpB -> httpB.disable())

                                // 로그인
                                .formLogin(form -> form
                                                .loginProcessingUrl("/api/v1/auth/login")
                                                .successHandler(memberAuthSuccessHandler)
                                                .failureHandler(customAuthenticationFailureHandler)
                                                .usernameParameter("memberId")
                                                .passwordParameter("password")
                                                .permitAll())
                                /*
                                 * .logout(logout -> logout
                                 * .logoutUrl("/api/v1/auth/logout") // 로그아웃 처리 URL
                                 * .logoutSuccessUrl("/api/v1/auth/loginForm?logout=1") // 로그아웃 성공 후 이동페이지
                                 * .deleteCookies("JSESSIONID") // 로그아웃 후 쿠키 삭제
                                 * .addLogoutHandler(jwtLogoutHandler)
                                 * .logoutSuccessHandler(apiLogoutSuccessHandler)
                                 * )
                                 */


                                // OAuth2 로그인 활성화
                                .oauth2Login(oauth2 -> oauth2 // formLogin 후 위치 유지
                                                .successHandler(oAuth2SuccessHandler)
                                                .failureUrl("/api/v1/auth/error") // 백엔드 경로로 변경
                                )

                                // 인증/인가 예외 처리
                                .exceptionHandling(handling -> handling
                                                .authenticationEntryPoint(customAuthenticationEntryPoint)
                                                .accessDeniedHandler(customAccessDeniedHandler))

                                // 필터 순서: JwtExceptionFilter -> JwtAuthenticationFilter ->
                                // UsernamePasswordAuthenticationFilter
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class) // [추가]

                                .build();// 필터 체인 빌드

        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
