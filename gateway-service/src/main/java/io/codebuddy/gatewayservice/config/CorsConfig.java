package io.codebuddy.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    // 브라우저를 통해 주입된 Origin 헤더가 cors 검사를 통과할 수 있도록 허용
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 Origin
        config.setAllowedOrigins(List.of(
                "https://closet-buddy.chlab.org",
                "http://localhost:5173",    // 로컬 개발용
                "http://localhost:80"
        ));

        // 허용할 HTTP 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 허용할 헤더
        config.setAllowedHeaders(List.of("*"));

        // 인증 정보 허용 (Authorization 헤더 등)
        config.setAllowCredentials(true);

        // preflight 캐싱을 설정하여 불필요하게 OPTION 요청을 하지 않도록 1시간 캐싱
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
