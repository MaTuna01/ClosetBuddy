package io.codebuddy.closetbuddy.domain.common.feign;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign Client 설정
 */
@Slf4j
@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    /**
     * 호출한 HTTP 요청의 Authorization 헤더를 feign 호출 시 그대로 전달
     * 결과 : Gateway에서 주입한 인증 정보(X-USER-ID, X-USER-ROLE)가 그대로 user-service에서 전달
     */
    @Bean
    public RequestInterceptor authHeaderInterceptor() {
        return (RequestTemplate template) -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if ( attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authorization = request.getHeader("Authorization");

                log.debug("Feign RequestInterceptor - Authorization 헤더 : {}",
                        authorization != null ? "존재 (길이: " + authorization.length() + ")" : "없음");

                if (authorization != null) {
                    template.header("Authorization", authorization);
                } else {
                    log.warn("Feign 호출 시 Authorization Header가 없습니다. 요청 URI : {}", request.getRequestURI());
                }
            }else {
                log.warn("Feign RequestInterceptor - RequestAttributes가 null입니다. RequestContextHolder에서 요청 정보를 가져올 수 없습니다.");
            }
        };
    }
}
