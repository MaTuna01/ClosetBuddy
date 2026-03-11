package io.codebuddy.userservice;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "custom.jwt.secrets.origin-key=test12345678901234567890123456789012",
        "custom.jwt.secrets.app-key=test98765432109876543210987654321098",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:testdb"
})
@ActiveProfiles("test")
public abstract class TestSupport {
}
