package io.codebuddy.closetbuddy.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class MemberClient {

    // RestClient는 빌더로 생성하거나 주입받음
    private final RestClient restClient = RestClient.create();

    public MemberDto getMemberInfo(String memberId) {
        return restClient.get()
                .uri("http://user-service/api/v1/members/me", memberId) // URL 설정
                .retrieve()
                .body(MemberDto.class); // 결과를 DTO로 바로 변환
    }
}