package io.codebuddy.gatewayservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class JwtTokenVerifier implements TokenVerifier {

    private final RestClient restClient;
    private final String userServiceBaseUrl;

    public JwtTokenVerifier(RestClient.Builder restClientBuilder,
                            @Value("${user-service.base-url:http://user-service}") String userServiceBaseUrl) {
        this.restClient = restClientBuilder.build();
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    @Override
    public VerifiedUser verify(String authHeader) {
        VerifiedUserResponse response = restClient.post()
                .uri(userServiceBaseUrl + "/api/v1/auth/verify")
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
