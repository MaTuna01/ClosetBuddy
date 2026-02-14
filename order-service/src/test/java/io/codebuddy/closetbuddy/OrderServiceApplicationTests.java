package io.codebuddy.closetbuddy;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled // DB 설정 없이 단위 테스트를 위해 전체 컨텍스트 로드를 제외합니다.
@SpringBootTest
class OrderServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
