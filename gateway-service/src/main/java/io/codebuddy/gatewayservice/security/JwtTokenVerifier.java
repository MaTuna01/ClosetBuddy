package io.codebuddy.gatewayservice.security;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class JwtTokenVerifier implements TokenVerifier {

    private final RestClient restClient;
    private final DiscoveryClient discoveryClient;

    public JwtTokenVerifier(RestClient.Builder restClientBuilder, DiscoveryClient discoveryClient) {
        this.restClient = restClientBuilder.build();
        this.discoveryClient = discoveryClient;
    }

    @Override
    public VerifiedUser verify(String authHeader) {
        // 유레카에서 "USER-SERVICE"라는 이름으로 등록된 서버 목록을 일기
        List<ServiceInstance> instances = discoveryClient.getInstances("USER-SERVICE");

        if (instances == null || instances.isEmpty()) {
            throw new IllegalStateException("USER-SERVICE is not available in Eureka!");
        }

        // 서버의 주소(URI) 세팅
        String userServiceUrl = instances.get(0).getUri().toString();

        VerifiedUserResponse response = restClient.post()
                .uri(userServiceUrl + "/api/v1/auth/verify") //verify 주소로 요청
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .body(VerifiedUserResponse.class);

        if (response == null) {
            throw new IllegalStateException("Empty verification response from user-service");
        }

        return new VerifiedUser(response.userId().toString(), response.role());
    }

    private record VerifiedUserResponse(Long userId, String role) {
    }
}